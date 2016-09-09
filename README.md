JIRA ANalyze
============

`jan` the JIRA ANlyze tool is a simple command line tool to extract
information from JIRA which are not fetchable via JIRA in a structured way.

*NOTE:* The tool is in early development stage so please use on your own risk.

`jan` is a command based tool which means it comes with a central starting command
(a.k.a `jan`) and subcommands.

Command parameters
==================

`jan`supports 3 main parameters. The main parameters supports the connectio 
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


Subcommands
-----------

*`transition`*

Fetch all relevant transition information for the tickets and print
the values to standard out.

The only parameter for the command is the JQL query to fetch the data
from remote JIRA.

Example:

        jan --password secret --login jirauser --jira https://example.com:8443 transition \
            'project = MyProject and type = Bug and (priority = Blocker or priority = Critical or priority = Major) ORDER BY key DESC'

Tip: Surround the query with apostrophe (U+0027) characters to avoid problems with
the command line interpreter (I use `bash` on a Mac).

FAQ
===

How to work with `https` URIs

> TBD

