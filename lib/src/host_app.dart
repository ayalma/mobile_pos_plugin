enum HostApp {
  FANAVA,
  IKC,
  PEC,
  NAVACO,
  SEP,
  SEPEHR,
  UNKNOWN,
}

HostApp parseHostApp(String hostApp) {
  switch (hostApp) {
    case 'FANAVA':
      return HostApp.FANAVA;
    case 'IKC':
      return HostApp.IKC;
    case 'PEC':
      return HostApp.PEC;
    case 'NAVACO':
      return HostApp.NAVACO;
    case 'SEP':
      return HostApp.SEP;
    case 'SEPEHR':
      return HostApp.SEPEHR;
    case 'UNKNOWN':
      return HostApp.UNKNOWN;
    default:
      throw ArgumentError('$hostApp is not a valid HostApp.');
  }
}
