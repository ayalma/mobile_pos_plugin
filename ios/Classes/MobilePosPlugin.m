#import "MobilePosPlugin.h"
#if __has_include(<mobile_pos_plugin/mobile_pos_plugin-Swift.h>)
#import <mobile_pos_plugin/mobile_pos_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "mobile_pos_plugin-Swift.h"
#endif

@implementation MobilePosPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftMobilePosPlugin registerWithRegistrar:registrar];
}
@end
