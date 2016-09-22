# Jira ANalyze

`jan` the Jira ANalyze tool is a simple command line tool to extract
information from JIRA which are not fetchable direct via JIRA in a structured
way.

*NOTE:* The tool is in early development stage so please use on your own risk.

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

# Configure

Add the `jan` directory to your `PATH` environment.

# Running `jan`

## Command parameters

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

### `transition`

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
> `> openssl x509 -in <(openssl s_client -connect www.google.com:443 -prexit 2>/dev/null)`
>
> The domain name and port (`www.google.com:443`) may differ on your system.
> Please contact your system administrator to get the correct data.
>
> The result may look like the following:
>
> `-----BEGIN CERTIFICATE-----` <br />
> `MIIEgDCCA2igAwIBAgIIK6L1O7WFg3UwDQYJKoZIhvcNAQELBQAwSTELMAkGA1UE`
> `BhMCVVMxEzARBgNVBAoTCkdvb2dsZSBJbmMxJTAjBgNVBAMTHEdvb2dsZSBJbnRl`
> `cm5ldCBBdXRob3JpdHkgRzIwHhcNMTYwOTE0MDgyMDQwWhcNMTYxMjA3MDgxOTAw`
> `WjBoMQswCQYDVQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwN`
> `TW91bnRhaW4gVmlldzETMBEGA1UECgwKR29vZ2xlIEluYzEXMBUGA1UEAwwOd3d3`
> `Lmdvb2dsZS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCAmQKH`
> `oFWTZ2HK2CN21xj8iEDvGoySc4qlPD3pmJpJFxO533WEPhnd//6QF+Krr3DMaLzS`
> `8dj0mzSTHRnBgecv8/IYtvwaon28S8fdTJExgEZopfyCRTxG8FZc2v5t5VLW9Cd+`
> `iuaQfAUTmAHcrq304XAjjaypJZ4ggnMzsJqaK2aRZUzVwzsADFjrMGjF9KGiu+Dy`
> `obsuATblK9rgFixm2NQipPtt4kRuJbPp9qpJMcZael30mrunV8FLLIgLgEWYUzhO`
> `+AOWeomKSHKsZO8akY7DFmSLpoY7A9BKHCByjbf9fwXuBFDrSPfpekYW2kT4NfHU`
> `8mBpqyyrSCIzEhv3AgMBAAGjggFLMIIBRzAdBgNVHSUEFjAUBggrBgEFBQcDAQYI`
> `KwYBBQUHAwIwGQYDVR0RBBIwEIIOd3d3Lmdvb2dsZS5jb20waAYIKwYBBQUHAQEE`
> `XDBaMCsGCCsGAQUFBzAChh9odHRwOi8vcGtpLmdvb2dsZS5jb20vR0lBRzIuY3J0`
> `MCsGCCsGAQUFBzABhh9odHRwOi8vY2xpZW50czEuZ29vZ2xlLmNvbS9vY3NwMB0G`
> `A1UdDgQWBBSlyYl+/3NDrJ9jQw7V9viRhoXF2jAMBgNVHRMBAf8EAjAAMB8GA1Ud`
> `IwQYMBaAFErdBhYbvPZotXb1gba7Yhq6WoEvMCEGA1UdIAQaMBgwDAYKKwYBBAHW`
> `eQIFATAIBgZngQwBAgIwMAYDVR0fBCkwJzAloCOgIYYfaHR0cDovL3BraS5nb29n`
> `bGUuY29tL0dJQUcyLmNybDANBgkqhkiG9w0BAQsFAAOCAQEAPSDolYdq1tkYsmeA`
> `z1Pmb2MwA23nhcyUTP00QYLMYYG/8CxGhUc1tonuzA/ws0uy+3z+vF/4UxsGTlJL`
> `yOMon68TU1OYGuGlQTLV7CUVb8K348dSz/Yv9zLGSX1CdD7OwurPKgSzyqfaLzgo`
> `sn9YycO0SHlVRYf9hADKthZXjprnXScJ/uYWYqRn36Yd70zojdLqak0DUl51quVy`
> `4s1VSauEEj76Prrq0L6HOw86CMIKWnLRTBGT7Y75g8ELD53H/j/rGJDrbFZOu3N0`
> `aPcpIifFRyflDAY52c3DaLBlKnx6OBXaFoJwXa54ncQs4DXhJvzNDqf+X6BgTJ9x`<br />
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
> Copy the certificate into a file (e.g. `~/cert`)
>
> Now create the trust store with the following command line:
>
> `> keytool -v -import -file ~/cert -alias jira -keystore ~/.jan/cacert -storepass changeit`
>
> That's it. If all was done fine, the JIRA `https` connections with `jan` will
> work fine. In case of additional trouble please contact your system administrator.
>
> *NOTE:* for reading from the trust store a password is not necessary.

## Why is `jan` only for Unix like systems?

> Because I've currently no Windows environment. But you are free to sponsor me
> a license :-)
