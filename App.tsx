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

console.log('SystemOverlayModule:', SystemOverlayModule); 

const notion = new Client({ auth: process.env.NOTION_API_KEY });

const checkOverlayPermission = async () => {
  if (Platform.OS === 'android') {
    try {
      const isGranted = await NativeModules.SystemOverlayModule.isOverlayPermissionGranted();
      console.log('Overlay Permission Granted:', isGranted);
      return isGranted;
    } catch (error) {
      console.error('Failed to check overlay permission:', error);
      return false;
    }
  } else {
    console.warn('Overlay permission is not required on iOS');
    return false;
  }
};

const handleFormSubmission = async (data: any) => {
  const formData = JSON.parse(data);
  console.log('Received Data:', formData);

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
        Category: {
          rich_text: [
            {
              text: {
                content: formData.inputCategory,
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
    console.log('Success! Entry added:', response);
  //   alert('Data submitted successfully!');
  } catch (error) {
    console.error('Error:', error);
  //   alert('Error submitting data. Please try again.');
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
        console.log('Overlay permission not granted. Prompting user to enable it.');
        Linking.openSettings();
      }else{
        console.log("permission is granted")
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
