import {Alert, Button, StyleSheet, Text, View} from 'react-native';
import React from 'react';
import EsewaService from './src/Esewa';

const clientId = 'JB0BBQ4aD0UqIThFJwAKBgAXEUkEGQUBBAwdOgABHD4DChwUAB0R';
const secretKey = 'BhwIWQQADhIYSxILExMcAgFXFhcOBwAKBgAXEQ==';

const App = () => {
  console.log({EsewaService});

  function generateRandomProductId() {
    let randomProductId = '';

    for (let i = 0; i < 10; i++) {
      randomProductId += Math.floor(Math.random() * 10);
    }

    console.log('ESewaModule:ProductId', randomProductId);
    return randomProductId;
  }

  const handlePayment = async () => {
    try {
      const TEST_ENVIRONMENT = await EsewaService.getEsewaTestEnvironment();

      console.log({TEST_ENVIRONMENT});
      const result = await EsewaService.pay(
        clientId,
        secretKey,
        TEST_ENVIRONMENT,
        '100',
        'pname',
        generateRandomProductId(),
        'https://youtube.com',
      );
      console.log(result);
      Alert.alert('Payment Successfull', result);

    } catch (error) {
      Alert.alert('Payment Error', error.message);
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Pay with eSewa" onPress={handlePayment} />
    </View>
  );
};

export default App;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

});
