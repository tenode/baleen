# Baleen Docker

This folder contains two approaches to using Baleen with Docker.

* A Dockerfile providing a 'Baleen Docker application'. This is a vanilla Baleen which you can configure and integrate with outside services if you need to, or you can just use it as a command line applicaiton to process files as the baleen-docker/example does.
* Docker Compose which run Baleen alongside Mongo and Elasticsearch. It provides a more comprehensive configuration example which allow you to use Baleen straight away with your documents you place in the baleen/data directory.

These Docker implementations are examples and should be further configured and secured for production use. The Docker version will use the Dockerfile to build an image.

In both cases you will wish to provide your own Baleen configuration, though a comprehensive example file is included to allow you to quickly get going.

Baleen (JARs) will be downloaded from the GitHub release pages.

# Simple Baleen Docker

You should [install Docker](https://docs.docker.com/installation/).

Then you can run the build image script:

    $ ./build-image.sh

This will download and build a Docker image called dstl/baleen.  The Dockerfile is in the baleen-docker directory.

To simplify running, use the baleen script from a directory containing a Baleen configuration. This will run Baleen as the current user. Press Ctrl+C to quit Baleen.

The /baleen-docker/example directory has an example configuration:

    $  cd baleen-docker/example
    $ ../../run-standalone.sh baleen.yml

The Baleen port 6413 will be exposed to the host. Visit http://localhost:6413.

Since referencing the run-standalone script is pain you either add it to your path (or copy somwhere within your path):

    $ sudo cp run-standalone.sh /usr/local/bin/baleen
    $ sudo chmod +x /usr/local/bin/baleen

Now you can simply run Baleen, in the directory with the configuration as:

    $  cd baleen-docker/example
    $ baleen baleen.yml

# Docker Compose

You should [install Docker](https://docs.docker.com/installation/) and [Docker compose](https://docs.docker.com/compose/install/).

To run as a service (in the background):

    $ docker-compose up -d

Or, to run and wait:

    $ docker-compose up

Both of these will download and build Elasticsearch and Mongo images, and configure Baleen to use them. The Elasticsearch and Mongo services will be available on the host machine on their standard ports (9200 and 27017 respectively).

Warning! To allow you to easily access Elasticsearch from an online tools of your choice, such as [ElasticHQ](http://www.elastichq.org/app/index.php), the docker-compose image has CORS enabled (the default is disabled). You can disable this by commenting out the command: "-Des.http.cors.enabled=true" or change the allowed origins (-Dhttp.cors.allow-origin) to just the tools are using. See https://github.com/elastic/elasticsearch/issues/7151 for more details.

You can configure Baleen by editting the files in the /baleen directory. The sample configuration provided will read from the baleen/data directory, process the data and download output to the Elasticseach and Mongo. For example, after running Baleen you can search for documents with urls using Elasticsearch:

    $ curl localhost:9200/baleen_index/_search?q=entities.type=Url\&pretty

Or just use search the text content of the documents:

  $ curl localhost:9200/baleen_index/_search?q=content=Baleen\&pretty

At the time of writing, Docker does not allow us to be notified when the databases are ready before starting Baleen.  You may see errors from Baleen (run `docker-compose logs baleen`) because it has started before Elasticsearch is ready to receive connections. If this happens, you should simply restart Baleen:

    $ docker-compose restart baleen


To stop all services and then delete the containers use:

    $ docker-compose stop
    $ docker-compose rm

The data from Elasticsearch and Mongo is stored in the data/elasticsearch and data/mongo directories, so it is not lost when docker-compose is stopped (`docker-compose stop`). You can delete the contents with the `./clean-data.sh` script (which may need to be run as root).

You should tailor the docker-compose.yml file to meet your requirements (ports, Elasticsearch cluster name, etc).
