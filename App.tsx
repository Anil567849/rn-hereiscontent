/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect } from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  useColorScheme,
  Platform, 
  Linking, 
  NativeModules, 
  DeviceEventEmitter,
  ToastAndroid,
  Alert
} from 'react-native';

const {SystemOverlayModule} = NativeModules;

import {
  Colors,
} from 'react-native/Libraries/NewAppScreen';

import { Client } from '@notionhq/client';
import WelcomeScreen from './pages/home/Home';

const notion = new Client({ auth: process.env.NOTION_API_KEY });

const checkOverlayPermission = async () => {
  if (Platform.OS === 'android') {
    try {
      const isGranted = await NativeModules.SystemOverlayModule.isOverlayPermissionGranted();
      return isGranted;
    } catch (error) {
      return false;
    }
  } else {
    return false;
  }
};

const handleFormSubmission = async (data: any) => {
  const formData = JSON.parse(data);

  try {
    const response = await notion.pages.create({
      parent: {
        database_id: process.env.NOTION_DB_ID as string,
      },
      properties: {
        Title: {
          title: [
            {
              text: {
                content: formData.inputTitle,
              },
            },
          ],
        },
        URL: {
          url: formData.inputUrl,
        },
        Tags: {
          rich_text: [
            {
              text: {
                content: formData.inputTags,
              },
            },
          ],
        },
        Description: {
          rich_text: [
            {
              text: {
                content: formData.inputDescription,
              },
            },
          ],
        },
        Platform: {
          rich_text: [
            {
              text: {
                content: formData.selectedPlatform,
              },
            },
          ],
        },
      },
    });

    if (response.id) { // If the response contains an `id`, it means the page was successfully created
      ToastAndroid.show('Saved in database', ToastAndroid.SHORT);
    } else {
      ToastAndroid.show('Not able to save in database', ToastAndroid.SHORT);
    }
  } catch (error) {
    ToastAndroid.show('Error saving to database', ToastAndroid.SHORT);
    console.error('Error:', error);
  }
};

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  function startApp() {
    Linking.openSettings().then(() => {
      // Add a slight delay to ensure the permission change is recognized
      setTimeout(async () => {
        const granted = await checkOverlayPermission();
        if (!granted) {
          ToastAndroid.show('Please Enable Overlay Permission', ToastAndroid.SHORT);
        } else {
          ToastAndroid.show('Got Overlay Permission', ToastAndroid.SHORT);
          await SystemOverlayModule.startOverlayService();
        }
      }, 1000);
    });
  }

  useEffect(() => {
    checkOverlayPermission().then(async (granted) => {
      if (granted) {
        await SystemOverlayModule.startOverlayService();
      } else {
        Alert.alert(
          'Permission Needed',
          'Please Enable Overlay Permission',
          [
            {
              text: 'Cancel',
              onPress: () => ToastAndroid.show('Overlay Permission Denied', ToastAndroid.SHORT),
              style: 'cancel',
            },
            {
              text: 'OK',
              onPress: () => startApp(),
            },
          ],
          { cancelable: false }
        );
      }
    });
  }, []);

  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener('onFormSubmit', (data) => {
        handleFormSubmission(data);
    });

    return () => {
        subscription.remove(); // Clean up the listener when the component unmounts
    };
  }, []);

  return (
    <SafeAreaView>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <WelcomeScreen />
    </SafeAreaView>
  );
}

export default App;
