# upload-maven-plugin
Maven plugin to upload file(s) with simple HTTP PUT/POST.

This was forked from the Sonatype/maven-upload-plugin, which they've abandoned.

[![Maven Central](https://img.shields.io/maven-central/v/net.lopht.maven-plugins/upload-maven-plugin.svg?style=plastic)](search.maven)
[![Build Status](https://travis-ci.org/lopht/upload-maven-plugin.svg)](https://travis-ci.org/lopht/upload-maven-plugin)

This plugin provides two goals:

* upload-file: Upload a single file.
* upload-files: Scans a base directory and uploads all files, filtered by include/exclude lists.

See [plugin docs](https://lopht.github.io/upload-maven-plugin) for details. 
