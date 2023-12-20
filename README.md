To implement EZDiagsSDK into your project follow the below steps

Step 1. Add the JitPack repository to your build file
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
  ```
 Step 2. Add the dependency
 ```gradle
 dependencies {
	        implementation 'com.github.ezshifa:EZDiagsSDK:1.0.1'
	}
  ```

Sample Usage
```gradle
  EZDiags.startScanning(Activity Context, "username here", "userkey here")
  ```
