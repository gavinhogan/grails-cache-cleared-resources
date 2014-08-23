package org.example


class BuildInfo {

    final static String FILE = 'project-build.properties'
    Properties buildProperties
    private static BuildInfo instance = new BuildInfo()
    static {
        instance.loadProperties()
    }

    void loadProperties(){
        if (buildProperties) return

        InputStream input = null;
        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE)
            if (input != null) {
                buildProperties = new Properties()
                buildProperties.load(input)
            }else{

            }

        }
        catch (Exception e) {
            throw new RuntimeException("Cannot load application metadata:" + e.getMessage(), e);
        }
    }


    public static getInstance(){
        return instance
    }

    String getVersionId(){
        loadProperties()
        def r = buildProperties?.getProperty('build.scmVersion')?:"default-revision"
        return r
    }
}
