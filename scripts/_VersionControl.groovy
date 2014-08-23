/*
 * Methods to facilitate easy access to version control within our Grails scripts.
 */

interface VersionControlProvider {
    boolean add(File file)
    String getCurrentRevision()
    String getCurrentBranch()
    String getName()
}

class FileSystemVersionControlProvider implements VersionControlProvider {
    boolean add(File file) {
        return true
    }
    String getCurrentRevision() {
        return null
    }
    String getCurrentBranch() {
        return null
    }
    String getName() {
        return 'File System'
    }
}


class MercurialVersionControlProvider implements VersionControlProvider {
    boolean add(File file) {
        boolean success = false
        try {
            def proc = "hg add ${file.path}".execute()
            proc.consumeProcessOutput(System.out, System.err)
            if (0 == proc.waitFor()) {
                success = true
            }
        } catch(Exception ex) {
            ex.printStackTrace(System.err)
        }
        return success
    }
    String getCurrentRevision() {
        def revision = null
        try {
            revision = 'hg log --rev . --limit 1 --template {node}'.execute().text.trim()
        } catch (Exception e) { }
        return revision
    }
    String getCurrentBranch() {
        def branch = null
        // First, try to find an active bookmark
        try {
            def branches = []
            'hg bookmarks'.execute().text.eachLine{branches << it}
            branch = branches.find{it.contains("*")}.tokenize()[1]
        } catch (Exception e) { }
        // Otherwise, look for a single bookmark on the current revision
        // If there are multiple, we don't know which one was intended
        if (!branch) {
            try {
                def branches = 'hg id --bookmarks'.execute().text.tokenize()
                if (branches.size() == 1) {
                    branch = branches[0]
                }
            } catch (Exception e) { }
        }
        return branch
    }
    String getName() {
        return 'Mercurial'
    }
}

class GitVersionControlProvider implements VersionControlProvider {
    boolean add(File file) {
        boolean success = false
        try {
            def proc = "git add ${file.path}".execute()
            proc.consumeProcessOutput(System.out, System.err)
            if (0 == proc.waitFor()) {
                success = true
            }
        } catch(Exception ex) {
            ex.printStackTrace(System.err)
        }
        return success
    }
    String getCurrentRevision() {
        def revision = null
        try {
            revision = "git rev-parse HEAD".execute().text.trim()
        } catch (Exception e) { }
        return revision
    }
    String getCurrentBranch() {
        def branch
        try {
            def branches = []
            'git branch'.execute().text.eachLine { branches << it.trim() }
            branch = branches.find { it =~ /^\*/ }?.replaceFirst(/^\*\s*/, '')
        } catch (Exception ignored) {
            branch = null
        }
        return branch
    }
    String getName() {
        return 'Git'
    }
}

boolean directoryExistsInSelfOrAncestor(File dir, String name) {
    if (new File(dir, name).isDirectory()) {
        return true
    }
    File parent = dir.getParentFile()
    if (parent != null) {
        return directoryExistsInSelfOrAncestor(parent, name)
    }
}

VersionControlProvider getVersionControlProvider() {
    if (directoryExistsInSelfOrAncestor(new File('.'), '.hg')) {
        return new MercurialVersionControlProvider()
    } else if (directoryExistsInSelfOrAncestor(new File('.'), '.git')) {
        return new GitVersionControlProvider()
    } else {
        return new FileSystemVersionControlProvider()
    }
}

target('initVersionControlProvider': 'Initializes a version control provider') {
    if (!binding.variables.containsKey("versionControlProviderInitialized")) {
        versionControlProvider = getVersionControlProvider()
        versionControlProviderInitialized = true
    }
}