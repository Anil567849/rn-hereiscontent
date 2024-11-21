import React from 'react';
import { View, Text, ImageBackground, StyleSheet, Dimensions } from 'react-native';

const { width, height } = Dimensions.get('window');

const WelcomeScreen = () => {
  return (
    <ImageBackground 
      source={require('../../assets/bg.jpg')} 
      style={styles.container}
    >
      <View style={styles.overlay}>
        <View style={styles.overlayText}>
            <Text style={styles.title}>Welcome to My App!</Text>
            <Text style={styles.subtitle}>A place where innovation happens</Text>
        </View>
      </View>
    </ImageBackground>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    width: width,
    height: height,
    position: 'absolute',
  },
  overlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.8)', // semi-transparent overlay
    width: '100%',
    height: '100%',
  },
  overlayText: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    width: '80%',
    height: '100%',
  },
  title: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 20,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 18,
    color: '#fff',
    marginBottom: 40,
    textAlign: 'center',
    paddingHorizontal: 30,
  },
  button: {
    backgroundColor: '#6200EE',
    paddingVertical: 15,
    paddingHorizontal: 40,
    borderRadius: 25,
  },
  buttonText: {
    fontSize: 18,
    color: '#fff',
    fontWeight: 'bold',
  },
});

export default WelcomeScreen;
