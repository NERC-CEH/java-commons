[![Build Status](https://travis-ci.org/NERC-CEH/java-commons.png?branch=master)](https://travis-ci.org/NERC-CEH/java-commons)

# CEH java-commons

A suite of common java components used for CEH applications. Below is a description of the various aspects which this project covers.

## Datastore api

The datastore api provides a mechanism to store, retrieve and version streams of data. It hooks in to the [Userstore api](#userstore-api) to provide an audit of who and when an input stream has changed. The reference implementation is powered by JGit, so ultimately creates a git repository.

For testing, we recommend that you join the datastore-git with a TemporaryFolder rule. This allows git repositories to be created per test and destroyed afterwards. The example below shows how to wire this up.

    public class GitDataRepositoryTest {
      public @Rule TemporaryFolder folder= new TemporaryFolder();
      private @Mock EventBus bus;
      private @Spy InMemoryUserStore<GitTestUser> userStore;
      private GitDataRepository<GitTestUser> dataStore;
      private final AnnotatedUserHelper factory;
      
      public GitDataRepositoryTest() {
          factory = new AnnotatedUserHelper(GitTestUser.class);
      }
      
      @Before
      public void createEmptyRepository() throws IOException, UsernameAlreadyTakenException {        
          //create an in memory userstore
          userStore = new InMemoryUserStore<>();
          populateTestUsers();
          
          //Init mocks
          MockitoAnnotations.initMocks(this);
          
          //create a testRepo folder and then a git data repository
          dataStore = new GitDataRepository(folder.getRoot(), userStore, factory, bus);
      }
    }


## Token Generation api

The token generation api defines a simple interface to allow creation of Tokens (essentially short lived messages). The token is intended to be secure such that it can be read by all but only understood by the token generator.

The default implementation is provides a way of generating tokens which requires no state to be maintained (other than a cryptographic key). Usage is pretty straight forward and is demonstrated below.

    String myMessage = "My Secret Message";
    int ttl = 1000; 
    
    // Generate a token
    Token generateToken = generator.generateToken(myMessage.getBytes(), ttl);

    // Read the token back to a message
    byte[] message = generator.getMessage(generateToken);

In the case of the stateless implementation, multiple token generators can read each others messages as long as they share the same cryptographic key.

Tokens are intended to be small messages, if larger messages are required consider using a different system.

## Userstore api

The userstore api provides a consistent wrapper around User and Group authentication mechanisms. The reference implementation of the userstore api is powered by [Atlassian Crowd](https://www.atlassian.com/software/crowd/overview) (see userstore-crowd). This implementation backs on to an instance of crowds rest api to perform user/group lookup and authentication.

A In Memory implementation of the userstore api is provided, however this is intended to be used for testing.

### User Attributes

During our application development, we have released that certain details which one application gathers and requires to be stored in a userstore do not apply to all applications. Therefore the userstore allows principals to store/create a subset of user attributes. The default way of doing this is to use an annotated Java Object and a **uk.ac.ceh.components.userstore.AnnotatedUserHelper**
This allows your application to create users in the following form:

    @Data // Lomboks data annotation
    public class MyUser implements User {
      @UserAttribute(USERNAME) private String username;
      @UserAttribute(EMAIL) private String email;
      @UserAttribute("customAttribute") private String myCustomAttribute;
      ...
    }

### Spring Security

A component is provided to hook the userstore-api to hook into spring security. An example javaconfig setup of this is provided below

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @EnableWebMvcSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
      @Autowired UserStore<CatalogueUser> userstore;
      @Autowired GroupStore<CatalogueUser> groupstore;
      
      @Override
      @Bean
      public AuthenticationManager authenticationManagerBean() {
        //Create an authentication manager which looks up the user based upon a preauthenticated token
        return new ProviderManager(Arrays.asList(new PreAuthenticatedUsernameAuthenticationProvider(userstore, groupstore)));
      }
      
      // Sample set up of a reading a header from a request and using this for login
      // DON'T DO THIS ON PUBLIC FACING APPLICATIONS, AS ANYONE CAN SET THE HEADER
      @Bean
      public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter remoteUserFilter = new RequestHeaderAuthenticationFilter();
        remoteUserFilter.setPrincipalRequestHeader("Remote-User");
        remoteUserFilter.setExceptionIfHeaderMissing(false);
        remoteUserFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        remoteUserFilter.setAuthenticationManager(authenticationManagerBean());
        return remoteUserFilter;
      }
      
      @Override
      public void configure(HttpSecurity http) throws Exception {
          http
            .sessionManagement()
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
              .addFilter(requestHeaderAuthenticationFilter())
            .anonymous()
              //If authentication fails, you can still set an anonymous principal
              .authenticationFilter(new AnonymousUserAuthenticationFilter("mySecretKey", PUBLIC_USER, "ROLE_ANONYMOUS"))
      }
    }

# Contributors

Christopher Johnson - cjohn@ceh.ac.uk