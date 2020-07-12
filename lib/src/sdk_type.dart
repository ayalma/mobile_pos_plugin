enum SdkType {
  Unknown,
  Rahyab,
  Pne,
}

extension SdkParser on SdkType {
  SdkType parse(String sdkType) {
    switch (sdkType) {
      case 'FANAVA':
        return SdkType.Rahyab;
      case 'IKC':
        return SdkType.Pne;
      case 'PEC':
        return SdkType.Unknown;

      default:
        throw ArgumentError('$sdkType is not a valid SdkType.');
    }
  }
}
