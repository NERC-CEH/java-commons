[![Build Status](https://travis-ci.org/NERC-CEH/java-commons.png?branch=master)](https://travis-ci.org/NERC-CEH/java-commons)

# CEH java-commons

A suite of common java components used for CEH applications. Below is a description of the various aspects which this project covers.

## PUBLISHING TO MAVEN CENTRAL

Gitlab CI pipeline does not work, need to do this locally.

Need to install GPG key (available in CI variables) to sign package. Copy secret key from CI variables to file _private.key_, then import.

    base64 -d private.key | gpg --import

Release the package to Maven Central via OSS Sonatype repository.

    mvn release:prepare release:perform -Dusername=oss -Dpassword={OSS access token} -Darguments=-Dgpg.keyname=chrisajohnson1988@gmail.com
    
Will be prompted for gpg.passphrase (available in CI variables)

Then need to go to https://oss.sonatype.org/ to release package. See https://central.sonatype.org/pages/releasing-the-deployment.html for details.

Need to login with _oss_ username and password.
* Under Build Promotion click Staging Repositories
* Select Repository, if it looks complete need to _Close_, _Refresh_, _Release_ to make the package available for use.

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

### EhCache Configuration in spring

The crowd userstore implementation is annotated with spring `@Cacheable` annotations to allow user details and group memberships to be cached locally for quick access. This is optional but **highly recommended**. To enable [EhCaching](http://www.ehcache.org/) additional dependencies will be required by you project. If managing with Maven then these will be:

    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${spring.version}</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>net.sf.ehcache</groupId>
        <artifactId>ehcache</artifactId>
        <version>2.10.2.2.21</version>
        <scope>provided</scope>
    </dependency>

With these in place, you will need to enable caching within spring. An example configuration might look like:

    @Configuration
    @EnableCaching
    public class CachingConfig {
        @Bean
        public CacheManager cacheManager() {
            return new EhCacheCacheManager(CrowdEhCacheSupport.createCacheManager());
        }
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

#### Kerb your enthusiasm

Kerberos is a computer network authentication protocol. If your users are part of an active directory, it is possible to use Kerberos so that these users automatically get logged in to your site. Meaning that these users only have to log in to their windows workstation.

In order for this to work a few things will need to be set up:

1. Your application will need a service account set up in active directory. This should be assigned a password which doesn't expire
2. An SPN from this service account in the form of **HTTP/your.domain.name**
3. A keytab file should be generated off of the **HTTP/your.domain.name** and supplied to the application server configured to automatically sign in with kerberos
4. A Global Policy will need to be set which adds your web address in to the Intranet Zone. You will also want to set a policy to allow Intranet Zone applications to **Automatic[ly] logon in Intranet Zone**

An issue with a system which allows automatic login to occur is that users can not logout. If you which to allow users to logout and/or log in with different credentials, you will likely want to register:

    uk.ac.ceh.components.userstore.springsecurity.SignoutRememberMeServices

The Kerberos extension was based upon work carried out in the [spring-security-kerberos](https://github.com/spring-projects/spring-security-kerberos) plugin.

#### Integrating with NTLMv2

NTLM is a proprietary authentication protocol used by Microsoft Windows. When using the *Negotiate* WWW-Authentication header the NTLM mechanism will be used in the event that the Kerberos protocol fails. Such an event can occur if the client workstation cannot communicate with the Key Distribution Center (e.g. Active Directory).

Since Microsoft recommends the use of Kerberos over NTLM, we have decided to exclude the Maven dependencies required to set up NTLM Authentication. These must be manually added to your project where required.
      
    <dependency>
      <groupId>ch.poweredge.ntlmv2-auth</groupId>
      <artifactId>ntlmv2-lib</artifactId>
      <version>1.0.5</version>
    </dependency>
  

In order to use NTLMv2 Authentication, you must be able to set up a Computer account in active directory and set a password. The Windows UI does not provide a method to do this so you will have to use something similar to the following script:

    'SetComputerPass.vbs (Obtained from https://www.liferay.com)
    Option Explicit

    Dim strDn, objPassword, strPassword, objComputer

    If WScript.arguments.count <> 1 Then
      WScript.Echo "Usage: SetComputerPass.vbs <ComputerDN>"
      WScript.Quit
    End If

    strDn = WScript.arguments.item(0)

    Set objPassword = CreateObject("ScriptPW.Password")
    WScript.StdOut.Write "Password:"
    strPassword = objPassword.GetPassword()

    Set objComputer = GetObject("LDAP://" & strDn)
    objComputer.SetPassword strPassword

    WScript.Quit

After you have a computer account in AD, you should be able to instantiate an NTLMManager and a NtlmAuthenticationFilter.
    
    # Note that the $ in the computer account is important
    NtlmManager ntlmManager = new NtlmManager("DOMAIN", "address.of.ad", "AD_LOCAL_NAME", "COMPUTER$@DOMAIN", "password");
    NtlmAuthenticationFilter filter = new NtlmAuthenticationFilter(authenticationManager(),ntlmManager);

**If you wish to use the Kerberos and the NTLM mechanisms in the same application, you will need to register the NTLM filter before the Kerberos one**

For a detailed discussion on the different messages which are passed around when using NTLM see [here](http://www.innovation.ch/personal/ronald/ntlm.html)

# Contributors

Christopher Johnson - cjohn@ceh.ac.uk
