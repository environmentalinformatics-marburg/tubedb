---
title: Documentation
category: "Documentation"
tags: "Documentation"
layout: single
sidebar:
  nav: "docs" 
---

## [Usage](usage)
{% for item in site.usage %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

## [Configuration](configuration)
{% for item in site.configuration %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

## [API](api)
{% for item in site.api %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

## Documentation
{% for item in site.documentation %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}
 


