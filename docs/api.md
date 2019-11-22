---
title: API
category: "api"
tags: "api"
layout: single
classes: wide
author_profile: false
sidebar:
  nav: "docs" 
---

TubeDB contains API functionality to query meta data, to process timeseries data and to create visualisations.

The API is a RESTful HTTP web service, that is easily accessible by most scripting or programming environments that are capable of retrieving data by HTTP GET-requests (e.g. JavaScript, R, Bash, Java). It is used by the TubeDB [web interface](../usage/web).

The R package [rTubeDB](../usage/rpackage) provides convenient bindings to R environment.

URLs are composed by base URL, API identifier, method name and query parameters.

syntax: `[base URL]/[API identifier]/[method name]?[query parameters]`

example: `http://localhost:8080/tsdb/sensor_list?plot=AEW09`

APIs
---

[Meta Data API](meta)

[Query API](query)