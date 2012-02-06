Nuxeo Yammer integration
=========================

This sample provides a simple example on how to use three legged oauth authentication and post a document on Yammer.

To use it you need to add a service provider with the following url and name:
url: https://www.yammer.com/api/v1/messages.json
name: Yammer_service_provider

The other parameters will be given to you by Yammer when you register a new application:
https://www.yammer.com/client_applications/new

Once you have register your application, you'll see a share in yammer widget on the summary tab of a Note document.
This will ask you to authorize nuxeo to access yammer. Then you'll be able to share document on yammer.

You can also use the ShareInYammer operation.
