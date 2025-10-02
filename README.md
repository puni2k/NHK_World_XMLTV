# NHK_World_XMLTV
This is a small programm that will read the EPG data from NHK World TV and parse it into XMLTV format.
Having the EPG data available in a XMLTV file makes it possible for the data to be imported into i.e. Tvheadend.

You can provide the path to a JSON configuration file as a command line argument.
The configuration file can contain the following parameters:
```
{
  "apiUrl": "...",
  "days": 1,
  "outputFile": "..."
}
```
## BREAKING CHANGES - October 2025:
NHK has revamped their program schedule page and therefore seems to have disabled the old API.
This tool has been updated to reflect these changes.

### apiUrl
URL of where to get the EPG JSON data. In the past you could make a single request for the desired timeframe but now it seems that you have to request data for each day separatly.

Default:
```
"https://masterpl.hls.nhkworld.jp/epg/w/%s.json"
```
Please note the placeholder is for the day you want to request the JSON EPG for. Format is ISO 8601 without separators.
i.e.  https://masterpl.hls.nhkworld.jp/epg/w/20251002.json
### days
Number of days for which to get EPG data. Only useful if you use the default API URL or provide a custom URL with placeholders.

Default: 1
### outputFile
Filepath for the generated XMLTV file.

Default: "out.xml"
