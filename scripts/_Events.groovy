includeTargets << new File("scripts/_VersionControl.groovy")

/* Runs every time grails starts the compiler */
eventCompileStart = {
    initVersionControlProvider()
    def buildProperties = createBuildProperties()
    storeBuildProperties(buildProperties)
}


Properties createBuildProperties(){
    def p = new Properties()
    def scmRevision = versionControlProvider.getCurrentRevision() ?: 'N/A'
    def scmBranch = versionControlProvider.getCurrentBranch() ?: 'N/A'
    def scmProvider = versionControlProvider.getName()
    p.'build.scmVersion' = scmRevision
    p.'build.scmBranch' = scmBranch
    p.'build.scmProvider' = scmProvider
    return p
}

void storeBuildProperties(Properties buildProperties){
    try {
        //println("Storing ${buildProperties}")
        def propFile = new File('src/java', 'project-build.properties')
        if(!propFile.exists()){
            propFile.createNewFile()
        }
        def fos = new FileOutputStream(propFile)
        buildProperties.store(fos, "Build Properties created by _Events.groovy")
        fos.flush()
        fos.close()

    } catch (Exception e) {
        println("*****UNABLE TO SAVE BUILD PROPS*****")
        println(buildProperties)
        e.printStackTrace()
    }
}