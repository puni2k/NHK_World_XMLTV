# NHK_World_XMLTV
This is a small programm that will parse the EPG data from NHK World TV and parse it into XMLTV format.
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
### apiUrl
URL of where to get the EPG JSON data. As of the current NHK World API you have to specify a time period in Unix time. You can specify a fixed value or let the program help you out with these by providing the URL in a String.format like way.

Default:
```
"https://nwapi.nhk.jp/nhkworld/epg/v7b/world/s%d-e%d.json"
```
Please note the two placeholders for start and end time that are used by the program
### days
Number of days for which to get EPG data. Only useful if you use the default API URL or provide a custom URL with placeholders.

Default: 1
### outputFile
Filepath for the generated XMLTV file.

Default: "out.xml"
