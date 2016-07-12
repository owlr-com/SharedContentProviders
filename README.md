# Work in progress / Concept Piece

Have multiple app? Need to share state between them (user details, favourites etc..). This is a
distributed `SharedPreferences` backed by a `ContentProvider`.

# Setup

Include the `providers` module in your project.

Add the following to your core app build config:
```
android {
  //...
  defaultConfig{
    manifestPlaceholders = [sharedPermission: "com.base.package.name.PERMISSION", sharedAuthorityMatcher: "com\\\\.base\\\\.package\\\\.(?:[a-z]{1,}\\\\.)*provider"]
  }
  //...
}
```
Missing these from your build config will break your build, they are **mandatory**.

Then use the `SharedSharedPreference` class as you would normally and the library will handle
distributing data.

```
SharedPreferences mPrefs = new SharedSharedPreference(mApplication);
```

### ManifestPlaceholders

We use the placeholders over Android Meta-data elements as the buildTools will fail at compile time
if you forget to include these.

**THESE MUST BE THE __SAME__ IN EVERY APP YOU PUBLISH**

- `sharedPermission` The unique permission to use. This is used to identify which providers can talk
to each other, this is combined with your app signature to make sure your apps can only talk to each
other.
- `sharedAuthorityMatcher` We use a regex method to find other providers. If your provider authority
matches the regex then your apps will talk to each other. Worth noting these need to be common before
deployment. E.g. use something like `com.company.{appname}`. That way you can find all your providers by default.

### ContentProvider SharedPreferences

The SharedProvider is backed by a normal SharedPreferences, you can override this by implementing
`SharedPreferencesProducer` on your `Application` object.

```
@Override
public @Nullable SharedPreferences provideSharedPreferences(){
  return context.getSharedPreferences("this_apps_local_store", Context.MODE_PRIVATE);
}
```

### Example Regex

To find each provider use package names which have common elements, now and in the future.

The default regex should be enough for you: `com\\\\.base\\\\.package\\\\.(?:[a-z]{1,}\\\\.)*provider`.
This matches:
- **com.base.package.**app1.**provider**
- **com.base.package.**provider
- **com.base.package.**app2.something.**provider**

Of course make sure you swap out the necessary parts of your common package name.

### Providers

Each app you create generates inherits a provider from the Library. (See snippet below).

```
<provider
        android:name="com.owlr.provider.SharedProvider"
        android:authorities="${applicationId}.provider"
        android:exported="true"
        android:permission="${sharedPermission}"
        />
```

### Permissions & Signatures

** All your apps need to be signed by the same Keystore**

The placeholder you provide for `sharedPermission` needs to be unique to your group of apps. The
`providers` and `BroadcastReceivers` rely on this `permission` and the fact that each app has the same
signature.


# Licence
>   Copyright 2016 OWLR Technologies Limited
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

