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
import NotionForm from './pages/home/_components/Form';

console.log('SystemOverlayModule:', SystemOverlayModule); 

const notion = new Client({ auth: process.env.NOTION_API_KEY });

const createNotionPage = async () => {

  const databaseId = '141ecc71627e80b5a472d0200ee150e9';
  const pageTitle = 'Testing';
  const pageContent = 'Content of the page';
  try {
    const response = await notion.pages.create({
      parent: {
        database_id: databaseId,
        // Alternatively, use this if you want to create in a page instead of database
        // page_id: parentPageId,
      },
      properties: {
        // This assumes your database has a "Name" property of type "title"
        Name: {
          title: [
            {
              text: {
                content: pageTitle,
              },
            },
          ],
        },
      },
      children: [
        {
          object: 'block',
          type: 'paragraph',
          paragraph: {
            rich_text: [
              {
                type: 'text',
                text: {
                  content: pageContent,
                },
              },
            ],
          },
        },
      ],
    });

    console.log('Success! Page created:', response);
    return response;
  } catch (error) {
    console.error('Error creating page:', error);
    throw error;
  }
};

const submitToNotion = async () => {
  try {
    const response = await notion.pages.create({
      parent: {
        database_id: '141ecc71627e80b5a472d0200ee150e9',
      },
      properties: {
        // Title field (title type)
        Title: {
          title: [
            {
              text: {
                content: "Sample Website",
              },
            },
          ],
        },
        // URL field (url type)
        URL: {
          url: "https://example.com",
        },
        // Category field (plain text)
        Category: {
          rich_text: [
            {
              text: {
                content: "Technology",
              },
            },
          ],
        },
        // Description field (plain text)
        Description: {
          rich_text: [
            {
              text: {
                content: "This is a sample description for the website.",
              },
            },
          ],
        },
        // Platform field (plain text)
        Platform: {
          rich_text: [
            {
              text: {
                content: "Web",
              },
            },
          ],
        },
      },
    });

    console.log('Success! Entry added:', response);
    return response;
  } catch (error) {
    console.error('Error details:', error);
    throw error;
  }
};

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

const handleFormSubmission = (formData: any) => {
  const parsedData = JSON.parse(formData);
  console.log('Received Data:', parsedData);
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
      <NotionForm />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
