
# ACR Phone Helper  (APH)
Source code of ACR Phone Helper (APH). Published in the interest of transparency.

APH is a companion app for ACR Phone. It records calls with the help of Accessibility API and shares them with ACR Phone.
You need ACR Phone for APH to work and you need APH to record calls with ACR Phone on Android 10+. Without APH, call recordings on Android 10+ will have no sound.

It feels like déjà vu! We had same problem couple of years before with ACR Call Recorder. At the time we thought creating a variant of ACR Call Recorder Called ACR Unchained would be a good idea but it confused many people. This time we are creating a companion app with a distinctive name. Hopefully people will understand the connection between the two apps.

Reason for creating APH and download link can be found at [https://acr.app](https://acr.app)   
ACR Phone can be downloaded from [https://play.google.com/store/apps/details?id=com.nll.cb](https://play.google.com/store/apps/details?id=com.nll.cb)

---

**Android 15 Update (24/April/2024)**
&nbsp;

As we have suspected before, Google might be preparing to put the final nail on the coffin of call recording and end the cat and mouse game for good.
&nbsp;
I have just read a [dooming article](https://www.androidauthority.com/android-15-enhanced-confirmation-mode-3436697/) about improvements to the “Enhanced Confirmation Mode”
I can confirm that **there will be no call recording possibility with Accessibility Service on Android 15**, if Google releases improved “Enhanced Confirmation Mode”.
&nbsp;

Improvements to “Enhanced Confirmation Mode” will prevent enabling Accessibility Service of any app that is not installed by a “Trusted Store” essentially ending possibility of call recording on Android 15+

---

Note to other developers.
- It is not possible to provide reproducible builds as APH needs to be signed with the same key ACR Phone is signed with.
- APH put together in around a week. Do not expect trendy architectural implementations.
- Code comments make references to main ACR Phone app which is not open source. Just ignore them.
- Client implementation is not open source but you can probably guess how it works by looking at the code.
- Never mind the spelling mistakes.
- Might accept pull requests.
---
Further details about Google's never-ending war against call recording
- [Possibly end of call recording for good on Android 15+](https://www.androidauthority.com/android-15-enhanced-confirmation-mode-3436697/)
- [https://nllapps.com/no](https://nllapps.com/no)
- [https://nllapps.com/android9](https://nllapps.com/android9) (Related to legacy ACR Call Recorder)
- [https://nllapps.com/android11](https://nllapps.com/android11) (Related to once in a lifetime opportunity that was missed)
- [https://nllapps.com/apps/acr/google-denies-phone-number-accesss.htm](https://nllapps.com/apps/acr/google-denies-phone-number-accesss.htm) (Related to Google's block on Call Log Access)