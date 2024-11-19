import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';
import {Picker} from '@react-native-picker/picker';
import { Client } from '@notionhq/client';

const notion = new Client({
  auth: process.env.NOTION_API_KEY,
});

const NotionForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    url: '',
    category: '',
    description: '',
  });
  const [selectedPlatform, setSlectedPlatform] = useState('Instagram');
  const [loading, setLoading] = useState(false);

  const platforms = [
    'Instagram',
    'Youtube',
    'Facebook',
    'Tik Tok',
    'X',
    'Reddit',
  ];

  const handleSubmit = async () => {
    setLoading(true);
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
                  content: formData.title,
                },
              },
            ],
          },
          URL: {
            url: formData.url,
          },
          Category: {
            rich_text: [
              {
                text: {
                  content: formData.category,
                },
              },
            ],
          },
          Description: {
            rich_text: [
              {
                text: {
                  content: formData.description,
                },
              },
            ],
          },
          Platform: {
            rich_text: [
              {
                text: {
                  content: selectedPlatform,
                },
              },
            ],
          },
        },
      });
      
      console.log('Success! Entry added:', response);
      // Clear form after successful submission
      setFormData({
        title: '',
        url: '',
        category: '',
        description: '',
      });
    //   alert('Data submitted successfully!');
    } catch (error) {
      console.error('Error:', error);
    //   alert('Error submitting data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView 
    style={styles.container}>
      <Text style={styles.header}>Add New Entry</Text>
      
      <Text style={styles.label}>Title *</Text>
      <TextInput
        style={styles.input}
        value={formData.title}
        onChangeText={(text) => setFormData({ ...formData, title: text })}
        placeholder="Enter title"
      />

      <Text style={styles.label}>URL *</Text>
      <TextInput
        style={styles.input}
        value={formData.url}
        onChangeText={(text) => setFormData({ ...formData, url: text })}
        placeholder="Enter URL"
        keyboardType="url"
      />

      <Text style={styles.label}>Category</Text>
      <TextInput
        style={styles.input}
        value={formData.category}
        onChangeText={(text) => setFormData({ ...formData, category: text })}
        placeholder="Enter category"
      />

      <Text style={styles.label}>Description</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        value={formData.description}
        onChangeText={(text) => setFormData({ ...formData, description: text })}
        placeholder="Enter description"
        multiline
        numberOfLines={4}
      />

      <Text style={styles.label}>Platform</Text>
      <View style={styles.pickerContainer}>
      <Picker
        selectedValue={selectedPlatform}
        onValueChange={(itemValue, itemIndex) =>
            setSlectedPlatform(itemValue)
        }>
        <Picker.Item label="Instagram" value="instagram" />
        <Picker.Item label="Youtube" value="youtube" />
        <Picker.Item label="LinkedIn" value="linkedin" />
        <Picker.Item label="Tik Tok" value="tiktok" />
        <Picker.Item label="Reddit" value="reddit" />
        <Picker.Item label="X" value="x" />
        </Picker>
      </View>

      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? 'Submitting...' : 'Submit'}
        </Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 20,
    backgroundColor: '#fff',
    height: '100%',
  },
  header: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333',
  },
  label: {
    fontSize: 16,
    marginBottom: 5,
    color: '#333',
    fontWeight: '500',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    marginBottom: 15,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  textArea: {
    height: 100,
    textAlignVertical: 'top',
  },
  pickerContainer: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    marginBottom: 15,
    backgroundColor: '#f9f9f9',
  },
  picker: {
    height: 50,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 10,
    marginBottom: 50
  },
  buttonDisabled: {
    backgroundColor: '#999',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default NotionForm;