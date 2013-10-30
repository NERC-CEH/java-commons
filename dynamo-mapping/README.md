# Dynamo Mapping


A dynamic mapping plugin for spring mvc powered by MapServer which uses 
freemarker templates for creating Map Files

Dynamo is basically a Spring MVC view resolver which processes templated map files and posts them to a map server instance (along with the Query String of the request) and returns the result to the client.

Once configured, Dynamo works just like any other spring mvc view resolver.

    @RequestMapping( "my-map/{service}")
    public ModelAndView getViewForMyMapService(
      @PathVariable("service") String serviceName,
      @RequestParam("customParameter") String custom
      ) {
      ...
      return new ModelAndView("my-templated-mapfile.map", model);
    }

