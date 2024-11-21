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
  DeviceEventEmitter
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
        database_id: '141ecc71627e80b5a472d0200ee150e9',
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
  } catch (error) {
    console.error('Error:', error);
  }
};

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {

    checkOverlayPermission().then(async (granted) => {
      if (!granted) {
        Linking.openSettings();
      }else{
        await SystemOverlayModule.startOverlayService();
      }
    });

  }, [])

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
