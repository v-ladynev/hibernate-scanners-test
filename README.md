# hibernate-scanners-test

It is just a test of various entities scanning approaches for Hibernate.

Test class — [ScannersTest.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/test/java/com/github/ladynev/scanners/ScannersTest.java)

### fluent-hibernate

If you are looking for a quick scanning approach without additional dependencies, you can try [fluent-hibernate](https://github.com/v-ladynev/fluent-hibernate) library (you will not need to have other jars, except the library) .
Apart this, it has some useful features for Hibernate 5 and Hibernate 4, including entities scanning, a Hibernate 5 implicit naming strategy, a nested transformer and others.

Just download the library from the project page: [fluent-hibernate](https://github.com/v-ladynev/fluent-hibernate) and use [EntityScanner](https://github.com/v-ladynev/fluent-hibernate/blob/master/fluent-hibernate-core/src/main/java/com/github/fluent/hibernate/cfg/scanner/EntityScanner.java):

_For Hibernate 4 and Hibernate 5:_
```Java
    Configuration configuration = new Configuration();
    EntityScanner.scanPackages("my.com.entities", "my.com.other.entities")
        .addTo(configuration);
    SessionFactory sessionFactory = configuration.buildSessionFactory();
```

_Using a new Hibernate 5 bootstrapping API:_
```Java
    List<Class<?>> classes = EntityScanner
            .scanPackages("my.com.entities", "my.com.other.entities").result();

    MetadataSources metadataSources = new MetadataSources();
    for (Class<?> annotatedClass : classes) {
        metadataSources.addAnnotatedClass(annotatedClass);
    }

    SessionFactory sessionFactory = metadataSources.buildMetadata()
        .buildSessionFactory();
```

### Links

[Can you find all classes in a package using reflection?](http://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection)

[How do you find all subclasses of a given class in Java?](http://stackoverflow.com/questions/492184/how-do-you-find-all-subclasses-of-a-given-class-in-java)

[Scanning Java annotations at runtime](http://stackoverflow.com/questions/259140/scanning-java-annotations-at-runtime)

[How can I enumerate all classes in a package and add them to a List?](http://stackoverflow.com/questions/176527/how-can-i-enumerate-all-classes-in-a-package-and-add-them-to-a-list)

[Hibernate Mapping Package](http://stackoverflow.com/questions/1413190/hibernate-mapping-package)

### Libraries
[fluent-hibernate](https://github.com/v-ladynev/fluent-hibernate) — [FluentHibernateLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/fluent/hibernate/cfg/scanner/FluentHibernateLibrary.java)

[google/guava](https://github.com/google/guava) (using `ClassPath`) — [GuavaLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/GuavaLibrary.java)

[spring-context](http://mvnrepository.com/artifact/org.springframework/spring-context) (using `ClassPathScanningCandidateComponentProvider`) — [SpringLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/SpringLibrary.java)

[spring-orm](http://mvnrepository.com/artifact/org.springframework/spring-orm) (using code form`LocalSessionFactoryBean`) — [SpringOrmLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/SpringOrmLibrary.java)
<br /><br />

[ronmamo/reflections](https://github.com/ronmamo/reflections) (improved version of `scannotation`) — [ReflectionsLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/ReflectionsLibrary.java)

[scannotation](http://scannotation.sourceforge.net/)
<br/><br/>

[ngocdaothanh/annovention](https://github.com/ngocdaothanh/annovention) (improved version of `annovention`) — [AnnoventionLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/AnnoventionLibrary.java)

[annovention](http://code.google.com/p/annovention)
<br /><br />

[lukehutch/fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner) — [FastClasspathScannerLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/FastClasspathScannerLibrary.java)

[rmuller/infomas-asl](https://github.com/rmuller/infomas-asl) — [InfomasAslLibrary.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/InfomasAslLibrary.java)

[ddopson/java-class-enumerator](https://github.com/ddopson/java-class-enumerator) — [ClassEnumeratorScanner.java](https://github.com/v-ladynev/hibernate-scanners-test/blob/master/src/main/java/com/github/ladynev/scanners/ClassEnumeratorScanner.java)
<br/><br/>

[classindex](https://github.com/atteo/classindex) (It uses indexes of classes are generated at compile-time)

[JBoss MC Scanning lib](https://developer.jboss.org/wiki/MCScanninglib)

### Sources

[ContextConfig.java](http://svn.apache.org/viewvc/tomcat/trunk/java/org/apache/catalina/startup/ContextConfig.java?annotate=1537835)  (from Tomcat)

[reflections/ClasspathHelper](https://github.com/ronmamo/reflections/blob/737683bb977d46b800621c6ef77afdbf1c294a54/src/test/java/org/reflections/ClasspathHelperTest.java) — contains some convenient methods to get urls for package, for class, for classloader


