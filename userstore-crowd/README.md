# Integration Testing the Crowd userstore

By default unit tests are disabled for the userstore-crowd component. To enable them, 
you must supply a crowd instance and empty directory which the tests can manipulate.

To do this you need to supply maven with the following parameters

		skipTests=false
		-Dcrowd-url=http://your.crowd.com:8095/crowd/rest/usermanagement/latest
		-Dcrowd-application=CROWD-APPLICATION-NAME
		-Dcrowd-password=CROWD-APPLICATION-PASSWORD