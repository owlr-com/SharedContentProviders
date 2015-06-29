# Work in progress / Concept Piece

The SharedProvider is backed by a normal SharedPreferences, this can be overridden by using `SharedPreferencesProvider`
on your `Application` object.

# Setup


```
android {
  //...
  defaultConfig{
    manifestPlaceholders = [sharedPermission: "com.base.package.name.PERMISSION", sharedAuthorityMatcher: "com\\\\.base\\\\.package\\\\.(?:[a-z]{1,}\\\\.)*provider"]
  }
  //...
}
```

### Providers

Each app you create generates a provider using your applicationId.

```
<provider
        android:name="com.owlr.provider.SharedProvider"
        android:authorities="${applicationId}.provider"
        android:exported="true"
        android:permission="${sharedPermission}"
        />
```

### ManifestPlaceholders

We use the placeholders over Android Meta-data elements as the buildTools will fail at compile time
if you forget to include these.

**THESE MUST BE THE SAME IN EVERY APP YOU PUBLISH**

- `sharedPermission` The unique permission to use. This is used to identify which providers can talk
to each other, this is combined with your app signature to make sure your apps can only talk to each
other.
- `sharedAuthorityMatcher` We use a regex method to find other providers. If your provider authority
matches the regex then your apps will talk to each other. Worth noting these need to be common before
deployment. E.g. use something like `com.company.{appname}`. That way you can find all your providers by default.

### Example Regex

To find each provider use package names which have common elements, now and in the future.

The default regex should be enough for you: `com\\\\.base\\\\.package\\\\.(?:[a-z]{1,}\\\\.)*provider`.
This matches:
- **com.base.package.**app1.**provider**
- **com.base.package.**provider
- **com.base.package.**app2.something.**provider**

Of course make sure you swap out the necessary parts of your common package name.