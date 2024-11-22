# Here is Content
### This is an app to help people to save the important content as you move through the different apps every single days. For eg: you might scroll through Instagram, TikTok, and Reddit every single day and stumble on some amazing content which might help you, Instead saving those content in particular app, you can use this app to save all the content in one place that is you notion's database directly through our application.


## Important Commands
check which devices are connected
``` bash
adb devices
```

check logs [.kt files: Log.d("debug", "this is testing debug")]
``` bash
adb logcat -s debug:D
```

install all npm packages
``` bash
npm install
```

run android app
``` bash
npm run android
```

clean android gradle file
``` bash
cd android && ./gradlew clean && cd ..
```

create debug app
``` bash
cd android && ./gradlew assembleDebug && cd ..
```

create android apk
``` bash
cd android && ./gradlew assembleRelease && cd ..
```


## How to use this application
### Step 1: 
Clone this application

### Step 2:
Goto: notion.so/profile
Create notion internal integration
Allow read and write permission
Copy secret

### Step 3:
Goto: notion.so
Create a new page
Create a database with required fields [Title, Url, Tags, Description, Platform]
Click on three dots on the top right corner
Click and connect to and choose your integration

### Step 4:
Goto you IDE and open our source code
Create .env file in you root directory
Paste your notion api key and database IDE
Check .env which keys to use
