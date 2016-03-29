# Elasticsearch Install Guide

## Install Elasticsearch

1. Download Elasticsearch 2.1.2 from https://www.elastic.co/downloads/past-releases/elasticsearch-2-1-2 
2. Extract the archive file
3. On Windows:
  1. Open a command prompt
  2. Navigate to the extracted Elasticsearch directory
  3. Run: `bin\service.bat install`
  4. Configure the newly installed service using Services
4. On OS X:
  1. Elasticsearch can be started by running `bin/elasticsearch`

## Install Plugins

The below commands can be run in Windows via the Command Prompt or OS X via a terminal session. The slashes might need to be updated.

1. Run: `bin/plugin install mobz/elasticsearch-head`
2. Run: `bin/plugin install analysis-phonetic`

## Configure Elasticsearch

The following link contains full documentation for all configuration options: http://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration.html. Below, I will mention the ones most likely to required updating.

1. Open config.yml from the Elasticsearch directory for editing
2. Set `cluster.name` and `node.name` to sensible values for the installation
3. Set `path.data` to the location where Elasticsearch will store its index files. This location should have an abundance of disk space.
4. Set `path.logs` to the location where Elasticsearch will store its log files.
5. By default, Elasticsearch can only be accessed on localhost, which is fine for development or if the ROAM Tomcat is installed on the same server. If Elasticsearch needs to be accessible from other servers, then update `network.bind_host: 0`. _Ensure that the server running Elasticsearch is not exposed to the internet before adjusting this value._
6. If the server running Elasticsearch does not have SSDs, then update `index.merge.scheduler.max_thread_count: 1`
