CLIENT SAATHI — ANDROID PROJECT (PHONE-ONLY DATA)

What this project includes
- Native Android client list, add/edit/delete, search, call button
- Local SQLite database stored on this phone only
- Follow-up date and time picker
- Android notification alarm and reboot rescheduling
- Permission shortcut for exact alarm settings
- GitHub Actions workflow to build a debug APK in the cloud

IMPORTANT
- This ZIP is Android source code, NOT a ready APK.
- APK build requires uploading this project to a GitHub repository and running its Actions workflow.
- Android may require notification permission and "Alarms & reminders" exact-alarm access.
- Battery saver / manufacturer background restrictions can still delay notifications. Test a reminder 2–3 minutes ahead before relying on it.
- App data is local to this phone. Uninstalling the app or clearing its data can erase records. No cloud sync is included.
- This is a starter app; test it carefully before entering real customer data. Keep client information private and secure.

BUILD FROM AN ANDROID PHONE
1. Download and extract this ZIP using a file manager that supports ZIP extraction.
2. Open https://github.com/ in Chrome and sign in/create an account.
3. Create a new repository, e.g. ClientSaathiAndroid.
4. Upload all extracted files/folders to the repository root. Keep the .github/workflows/build-apk.yml file in that exact path.
   Tip: GitHub's web upload may not handle a whole folder well on mobile. If it is difficult, use GitHub's "Add file" flow to upload files individually, preserving folder paths, or use a browser that supports desktop site.
5. Open the repository's Actions tab. Select "Build Client Saathi APK" and tap Run workflow (or push files to main to trigger it).
6. Wait until the workflow finishes with a green check. Open that workflow run, scroll to Artifacts, and download "Client-Saathi-APK".
7. Extract the downloaded artifact ZIP to get app-debug.apk. Tap the APK to install. Android may ask to allow installs from that browser/file manager.
8. Open Client Saathi, allow notifications, then tap "Reminder permissions" and allow Alarms & reminders if offered.
9. Create a test client with a reminder a few minutes ahead. Keep the app installed and disable aggressive battery restriction for the app if your phone has that setting.

If GitHub mobile upload is too difficult, this source project needs a computer/developer build service to turn it into an APK; this chat cannot directly sign and compile the APK.
