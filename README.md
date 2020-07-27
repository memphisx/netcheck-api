[![NetCheck](https://github.com/memphisx/netcheck-frontend/raw/master/src/assets/netcheck-logo.png)](https://ncheck.eu)
<!-- prettier-ignore-start -->
<p align="center">
  <a aria-label="License" href="https://github.com/memphisx/netcheck-api/blob/master/LICENSE">
    <img alt="" src="https://img.shields.io/github/license/memphisx/netcheck-api?style=for-the-badge&labelColor=000000&color=blue">
  </a>
  <a aria-label="Docker image version" href="https://hub.docker.com/repository/docker/memphisx/netcheck-api">
    <img alt="" src="https://img.shields.io/docker/v/memphisx/netcheck-api/latest?style=for-the-badge&label=Version">
  </a>
  <img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/memphisx/netcheck-api?style=for-the-badge">
  <img alt="GitHub milestone" src="https://img.shields.io/github/milestones/progress-percent/memphisx/netcheck-api/1?style=for-the-badge&label=V1 Completion">
  <img alt="Docker Image Size (latest semver)" src="https://img.shields.io/docker/image-size/memphisx/netcheck-api?style=for-the-badge&label=Docker Image size">
  <a href="https://github.com/memphisx/netcheck-api/issues">
    <img alt="GitHub issues" src="https://img.shields.io/github/issues/memphisx/netcheck-api?style=for-the-badge">
  </a>
  <img alt="GitHub issues by-label" src="https://img.shields.io/github/issues/memphisx/netcheck-api/bug?style=for-the-badge">
  <a href="https://github.com/memphisx/netcheck-api/actions">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/memphisx/netcheck-api/Run%20Unit%20Tests?style=for-the-badge">
  </a>
    <a href="https://github.com/memphisx/netcheck-api/stargazers">
      <img alt="GitHub stars" src="https://img.shields.io/github/stars/memphisx/netcheck-api?style=for-the-badge">
    </a>
    <a href="https://github.com/memphisx/netcheck-api/network">
      <img alt="GitHub forks" src="https://img.shields.io/github/forks/memphisx/netcheck-api?style=for-the-badge">
    </a>
    <img alt="GitHub watchers" src="https://img.shields.io/github/watchers/memphisx/netcheck-api?style=for-the-badge">
  <img alt="GitHub language count" src="https://img.shields.io/github/languages/count/memphisx/netcheck-api?style=for-the-badge">
</p>
<!-- prettier-ignore-end -->


<br>

<h1 align="center">Selfhosted performance and availability monitoring app</h1>
<h3 align="center">Built on Spring Boot & Quasar Frameworks</h3>
<h3 align="center"><a href="https://demo.ncheck.eu" target="_blank">Check out the demo</a></h3>

<br>

Netcheck lets you monitor the availability and performance of your websites or web services by running on your own 
servers/machines or hosting providers of your preference. 
It is inspired by services like [Pingdom](https://www.pingdom.com) and [Statuscake](https://www.statuscake.com) 

This git repository is for the api/backend side of the app. For an easy way to control the app and check the state and performance 
of your websites check out the [Netcheck Frontend Repository](https://github.com/memphisx/netcheck-frontend) and its instructions on how to run it. 

**WARNING**: The app is still under heavy development and quite rough around the edges.
Feel free to provide feature requests or any feedback through the 
[github issues page](https://github.com/memphisx/netcheck-api/issues).

<br>

### Quick Start

The fastest way to run Netcheck is through docker, and the provided docker-compose.yml file to get all the required dependancies up and running

#### Using docker (recommended)

* Follow the official documentation to install [Docker](https://docs.docker.com/get-docker/) 
and [docker-compose](https://docs.docker.com/compose/install/).
* copy `database.env.example` to `database.env` and change the credentials for the db to something more appropriate.
This env file is going to be used both by the postgress docker container to init the db as well as the netcheck app 
in order to connect to it. 
* copy `.env.example` to `.env`. The default values are enough for connecting to the db in the previous step. 
If you want Notifications support through pushover change the `SETTINGS_NOTIFICATIONS_PUSHOVER_ENABLED` to `true` 
and fill the `SETTINGS_NOTIFICATIONS_PUSHOVER_APITOKEN` and `SETTINGS_NOTIFICATIONS_PUSHOVER_USERIDTOKEN`
with the values you are going to get from [Pushover after creating a new app](https://pushover.net/apps/build).
* Execute `docker-compose up -d` to bring everything up.
* Visit [http://127.0.0.1:8080/docs/v1/](http://127.0.0.1:8080/docs/v1/) from your favorite browser to check what 
you can do with the api or bring up the [Netcheck frontend](https://github.com/memphisx/netcheck-frontend) 
by following the instructions in the [README file](https://github.com/memphisx/netcheck-frontend/blob/develop/README.md)

#### Running locally
TODO

#### Running publicly through traefik reverse proxy
TODO

<br><br>

**Features:**<br>
⚡️ Built on Spring Boot (backend - this repo) and Quasar (<a href ="">Frontend</a>)<br>
⚡️ Support live checking of domains through HTTP and HTTPS<br>
⚡️ Support scheduled checks with predefined check frequency of 5,10 or 15 minutes<br>
⚡️ HTTPS Certificate validation checks<br>
⚡️ Metric Generation for uptime and response times<br>
⚡️ State change notification system (currently supports <a href="https://pushover.net/">Pushover</a>)<br>

**Other key features coming:**<br>
⚡️ Authentication <br>
⚡️ Multi user support<br>
⚡️ Additional notification providers (Pushbullet, Telegram, Discord, Slack, Kafka, etc)<br>
⚡️ Custom endpoint checks<br>
⚡️ Custom request headers on checks<br>
⚡ Additional check implementations<br>
⚡️ Multi location support for the scheduled checks<br>

<br>

### What is Netcheck Designed For?

Netcheck is designed for realtime monitoring and performance metric collection of websites through scheduled checks. 
It is similar to 3rd party services like [Pingdom](https://pingdom.com), [DownDetector](https://downdetector.co.uk) 
and [StatusCake](https://statuscake.com). 

Netcheck may not include the vast feature set the aforementioned services provide, but it gives you full control 
over your data and doesn't lock you in to a single provider. And since it is self hosted it can also run exclusively 
on your intranet/cluster and check non publicly available services.

<br>