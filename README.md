# Jira ANalyze

`jan` the Jira ANalyze tool is a simple command line tool to extract
information from JIRA which are not fetchable direct via JIRA in a structured
way.

`jan` is a command based tool which means it comes with a central starting
command (a.k.a `jan`) and subcommands from command line.


# Installing `jan`

## Precondition

`jan` requires a Java 8 runtime environment at least.

## Download `jan`

Download latest `jan` version from [here](http://www.speexx.de/jan/LATEST).

Unzip the archive in a folder of your choice with 

> `> unzip jan-X-bin.zip`

were X is the version of `jan`

## Configure

Add the `jan` directory to your `PATH` environment.

# Using `jan`

## Main parameters

`jan`supports 3 main parameters. The main parameters supports the connection 
of the subcommands against a remote JIRA instance.

    h, --help
       Prints the help page.
       Default: false
    -j, --jira
       JIRA connection URI.
    -p, --password
       JIRA connection password.
    -u, --user
       JIRA connection user.


## Subcommands

### `count`

Returns the count of the results of a JQL query. The result is printed to
standard out.

Example:

        > jan --password secret --login jirauser --jira https://example.com:8443 count \
              'project = MyProject and type = Bug and status = Closed'

The query prints a numeric value like `1234`.

### `issueanalyze`

Analyze the fields returning by the given JQL query for field names and field types.

The return information is formatted in JSON and printed to standard out.

### `issuequery`

A general purpose command to get JQL query results from JIRA in a CSV format. The
command support the current field values of the issues as well as the changelog
history of fields.

#### Command line parameter

The `issuequery` support 4 command line parameters to configure the query and the
result.

##### `--current`

The parameter takes a list of fields and field pathes to get the current values.
The parameter values are space delimited. In case of a field name with a space,
the parameter value must be surrounded by quotation mark characters (`U-0022`).

In JIRA fields can be nested. This means a field can have additional fields. In
such case the field must be described with a a field name path. The first part
of the field name path is the *root* field. An undefined list of child path
elements can follow. To distinguish between the path elements two colon (`U+003a`)
characters are used as field path name delimiter.

For example:

To get the *reporter* of an issue the parameter value looks like the following:

        reporter

To get the name of the *creator* of an issue the parameter looks like the following:

        creator::name

The parameter value referes to the issue field with name *creator* and the attribute
*name*. 

The reason for the is the field type. The *reporter* is a core field in a JIRA
issue where the *creator* is not core field.

The fields *key/issuekey* and *createdDate* are implicit and must not been
defined.

The parameter value is case insensitive.


###### Header name format

In case of a field name path the name of the header is the same as the field
path name where the field path name delimiter is replaced with one underscore 
(`U+005f`) character.


##### `--history`

The parameter takes a list of field names of the changelog to be exported.
E.g. if the field name is 

        summary

the result contains a list of *from* and *to* values of the summary. Additonal
the output may contain the timestamp of the change and/or the duration from
the previous change. See more at `--temporal` command line parameter.

The `--history` parameter doesn't support field name pathes.

The parameter value is case insensitive.

##### `--temporal`

The parameter is only required if and only if `--history` field are defined.
The parameter defines the temporal information in the output. It has 4 possible
values.

<table>
  <tr><th>Parameter value</th><th>Description</th><th>Default</th><th>Abbreviation</th></tr>
  <tr><td><tt>none</tt></td><td>No temporal output is available for the changed field.</td><td>no</td><td><tt>n</tt></td></tr>
  <tr><td><tt>time</tt></td><td>The timestamp of the change is part of the output.</td><td>no</td><td><tt>t</tt></td></tr>
  <tr>
    <td><tt>duration</tt></td><td>The duration in milliseconds since the previous change is part of the output.
      For the first duration value the value of <em>createdData</em> of the issue is used.<br />
      Duration is a simplification for working with changelog information in dependend tools of the chain.
    </td>
    <td>yes</td><td><tt>d</tt></td></tr>
  <tr><td><tt>both</tt></td><td>Both temporal information (<tt>time</tt> and <tt>duration</tt>) are part of the output.</td><td>no</td><td><tt>b</tt></td></tr>
</table>

##### `--query`

JQL query to get the data from JIRA.

#### Output

The implementation currently writes the output as [CSV](https://en.wikipedia.org/wiki/Comma-separated_values "CSV at Wikipedia")
format ([RFC 4180](https://www.ietf.org/rfc/rfc4180.txt)) with header to `stdout`. 
The first column should contain the issue *key*. The second column should contain
the *createdDate* value. It is recom

In case of changelog information in the output the names of the headers will have
a prefix to the field name.

<table>
    <caption>CSV changelog output prefix for example field name "issuetype"</caption>
    <tr><th>Column type</th><th>Prefix</th><th>Example</th></tr>
    <tr><td>From value</td><td><tt>from_</tt></td><td><tt>from_issuetype</tt></td></tr>
    <tr><td>Change date value</td><td><tt>at_</tt></td><td><tt>at_issuetype</tt></td></tr>
    <tr><td>Duration value</td><td><tt>duration_</tt></td><td><tt>duration_issuetype</tt></td></tr>
    <tr><td>To value</td><td><tt>to_</tt></td><td><tt>to_issuetype</tt></td></tr>
</table>

Every changelog entry gets an own line in the CSV file. In case of more then one
changelog field to export, every block gets its own lines but the blocks which
are not the actual changelog field contains not values for the column.

The full set of current fields are available for every changelog line.

##### Datetime output

In case of datetime information the datetime format follows the human readable
[ISO 8601](https://en.wikipedia.org/wiki/ISO_8601 "ISO 8601 at Wikipedia") format
with milliseconds but no timezone information.

E.g. `2016-12-01T18:12:45.432`

### `transitions`

Fetch all relevant transition information for the tickets and print
the values as [CSV](https://en.wikipedia.org/wiki/Comma-separated_values "CSV at Wikipedia")
to standard out.

The only parameter for the command is the JQL query to fetch the data
from remote JIRA.

Example:

        > jan --password secret --login jirauser --jira https://example.com:8443 transition \
              'project = MyProject and type = Bug and (priority = Blocker or priority = Critical or priority = Major) ORDER BY key DESC'

Tip: Surround the query with apostrophe (U+0027) characters to avoid problems with
the command line interpreter (I use `bash` on a Mac).

# FAQ

## How can I work with `https` JIRA URIs

> If running `jan` running against a JIRA instance protected with SSL, `jan`
> requires a trust store with the SSL certificate. `jan` requires this file
> in the user home directory - environment variable `HOME` - under `~/.jan/cacert`
> 
> You can set a different trust store with the environment variable `JAN_TRUST_STORE`.

## How can I setup a trust store

> There are many dfferent types of how to configure your system with a trust store.
> I show you here how I did it in the past.
>
> First there must be an installed `openssl`. That's the case on many Linux systems
> and Mac OS.
>
> Get the SSL certificate with the following command line:
>
> `> openssl s_client -connect example.com:443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ~/public.crt`
>
> The domain name and port (`example.com:443`) may differ on your system.
> Please contact your system administrator to get the correct data.
>
> The result may look like the following:
>
> `-----BEGIN CERTIFICATE-----` <br />
> `MIIF8jCCBNqgAwIBAgIQDmTF+8I2reFLFyrrQceMsDANBgkqhkiG9w0BAQsFADBw`
> `MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3`
> `d3cuZGlnaWNlcnQuY29tMS8wLQYDVQQDEyZEaWdpQ2VydCBTSEEyIEhpZ2ggQXNz`
> `dXJhbmNlIFNlcnZlciBDQTAeFw0xNTExMDMwMDAwMDBaFw0xODExMjgxMjAwMDBa`
> `MIGlMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxML`
> `TG9zIEFuZ2VsZXMxPDA6BgNVBAoTM0ludGVybmV0IENvcnBvcmF0aW9uIGZvciBB`
> `c3NpZ25lZCBOYW1lcyBhbmQgTnVtYmVyczETMBEGA1UECxMKVGVjaG5vbG9neTEY`
> `MBYGA1UEAxMPd3d3LmV4YW1wbGUub3JnMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A`
> `MIIBCgKCAQEAs0CWL2FjPiXBl61lRfvvE0KzLJmG9LWAC3bcBjgsH6NiVVo2dt6u`
> `Xfzi5bTm7F3K7srfUBYkLO78mraM9qizrHoIeyofrV/n+pZZJauQsPjCPxMEJnRo`
> `D8Z4KpWKX0LyDu1SputoI4nlQ/htEhtiQnuoBfNZxF7WxcxGwEsZuS1KcXIkHl5V`
> `RJOreKFHTaXcB1qcZ/QRaBIv0yhxvK1yBTwWddT4cli6GfHcCe3xGMaSL328Fgs3`
> `jYrvG29PueB6VJi/tbbPu6qTfwp/H1brqdjh29U52Bhb0fJkM9DWxCP/Cattcc7a`
> `z8EXnCO+LK8vkhw/kAiJWPKx4RBvgy73nwIDAQABo4ICUDCCAkwwHwYDVR0jBBgw`
> `FoAUUWj/kK8CB3U8zNllZGKiErhZcjswHQYDVR0OBBYEFKZPYB4fLdHn8SOgKpUW`
> `5Oia6m5IMIGBBgNVHREEejB4gg93d3cuZXhhbXBsZS5vcmeCC2V4YW1wbGUuY29t`
> `ggtleGFtcGxlLmVkdYILZXhhbXBsZS5uZXSCC2V4YW1wbGUub3Jngg93d3cuZXhh`
> `bXBsZS5jb22CD3d3dy5leGFtcGxlLmVkdYIPd3d3LmV4YW1wbGUubmV0MA4GA1Ud`
> `DwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwdQYDVR0f`
> `BG4wbDA0oDKgMIYuaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL3NoYTItaGEtc2Vy`
> `dmVyLWc0LmNybDA0oDKgMIYuaHR0cDovL2NybDQuZGlnaWNlcnQuY29tL3NoYTIt`
> `aGEtc2VydmVyLWc0LmNybDBMBgNVHSAERTBDMDcGCWCGSAGG/WwBATAqMCgGCCsG`
> `AQUFBwIBFhxodHRwczovL3d3dy5kaWdpY2VydC5jb20vQ1BTMAgGBmeBDAECAjCB`
> `gwYIKwYBBQUHAQEEdzB1MCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2Vy`
> `dC5jb20wTQYIKwYBBQUHMAKGQWh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9E`
> `aWdpQ2VydFNIQTJIaWdoQXNzdXJhbmNlU2VydmVyQ0EuY3J0MAwGA1UdEwEB/wQC`
> `MAAwDQYJKoZIhvcNAQELBQADggEBAISomhGn2L0LJn5SJHuyVZ3qMIlRCIdvqe0Q`
> `6ls+C8ctRwRO3UU3x8q8OH+2ahxlQmpzdC5al4XQzJLiLjiJ2Q1p+hub8MFiMmVP`
> `PZjb2tZm2ipWVuMRM+zgpRVM6nVJ9F3vFfUSHOb4/JsEIUvPY+d8/Krc+kPQwLvy`
> `ieqRbcuFjmqfyPmUv1U9QoI4TQikpw7TZU0zYZANP4C/gj4Ry48/znmUaRvy2kvI`
> `l7gRQ21qJTK5suoiYoYNo3J9T+pXPGU7Lydz/HwW+w0DpArtAaukI8aNX4ohFUKS`
> `wDSiIIWIWJiJGbEeIO0TIFwEVWTOnbNl/faPXpk5IRXicapqiII=`<br />
> `jaMH7A==`<br />
> `-----END CERTIFICATE-----`
>
> You can trust now the cerificate or validate it. Nevertheless, the validation
> process is out of the scope of this FAQ.
>
> Now create the `~/.jan/cacert` trust store. First create the `~/.jan` folder
> with 
>
> `> cd`<br />
> `> mkdir .jan`</br />
>
> Now create the trust store with the following command line:
>
> `> keytool -v -import -file ~/public.crt -alias jira -keystore ~/.jan/cacert -storepass changeit`
>
> That's it. If all was done fine, the JIRA `https` connections with `jan` will
> work fine. In case of additional trouble please contact your system administrator.
>
> *NOTE:* for reading from the trust store a password is not necessary.

## Why is `jan` only for Unix like systems?

> Because I've currently no Windows environment. But you are free to sponsor me
> a license :-)
