# Managing Delete Cascade On External Domains [](id=managing-delete-cascade-on-external-domains)

Pre/Post Actions can be very useful for implementing cascade logic between modules inherent in different domains.
It has been mentioned several times that to best implement domain driven design concepts, it is not recommended to use objects for relationships between entities inherent to different domains (projects), while , in these cases, it is strongly recommended to use ids as a mechanism to track relationships.
HyperIoT Framework's ActionListeners represent a good model for "listening" to what is happening on other project domains. 
In fact, although today the mechanism for invoking actions is local to the instance , in future versions pre/post notifications will be developed also leveraging technologies such as Kafka thus allowing the notification to reach all instances of a cluster within which different services may be running, just as if it were a Saga Pattern.

This approach will allow each deployed domain to be self-consistent but still receive information from events happening on other domains and behave accordingly.

