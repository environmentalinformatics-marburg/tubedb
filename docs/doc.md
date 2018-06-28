---
title: Documentation
category: "Documentation"
tags: "Documentation"
layout: single
sidebar:
  nav: "docs" 
---

{% for item in site.documentation %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}


## Installation
{% for item in site.installation %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

## Functionality
{% for item in site.functionality %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

## R Interface
{% for item in site.r %}
  <p><a href="{{ item.url | prepend: site.baseurl}}">{{ item.title }}</a></p>
{% endfor %}

