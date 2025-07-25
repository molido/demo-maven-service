package org.devops


//获取POM中的坐标
def GetGav(){
   //上传制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    env.jarName = jarName - "\n"
    
    // def pom = readMavenPom file: 'pom.xml'
    // env.pomVersion = "${pom.version}"
    // env.pomArtifact = "${pom.artifactId}"
    // env.pomPackaging = "${pom.packaging}"
    // env.pomGroupId = "${pom.groupId}"


    env.pomVersion = sh(script: "grep '<version>' pom.xml | head -1 | sed -e 's/.*<version>//' -e 's#</version>.*##'", returnStdout: true).trim()
    env.pomArtifact = sh(script: "grep '<artifactId>' pom.xml | head -1 | sed -e 's/.*<artifactId>//' -e 's#</artifactId>.*##'", returnStdout: true).trim()
    env.pomGroupId = sh(script: "grep '<groupId>' pom.xml | head -1 | sed -e 's/.*<groupId>//' -e 's#</groupId>.*##'", returnStdout: true).trim()
    env.pomPackaging = sh(script: "grep '<packaging>' pom.xml | head -1 | sed -e 's/.*<packaging>//' -e 's#</packaging>.*##'", returnStdout: true).trim()

    
    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")

    return ["${pomGroupId}","${pomArtifact}","${pomVersion}","${pomPackaging}"]
}


//Nexus plugin deploy
def NexusUpload(){
    //use nexus plugin
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                                        classifier: '', 
                                        file: "${filePath}", 
                                        type: "${pomPackaging}"]], 
                            credentialsId: 'nexus-admin-user', 
                            groupId: "${pomGroupId}", 
                            nexusUrl: '10.67.47.94:30083',
                            nexusVersion: 'nexus3', 
                            protocol: 'http', 
                            repository: "${repoName}", 
                            version: "${pomVersion}"
}

//mvn deploy
def MavenUpload(){          
    def mvnHome = tool "maven"
    sh  """ 
        cd target/
        ${mvnHome}/bin/mvn deploy:deploy-file -Dmaven.test.skip=true  \
                                -Dfile=${jarName} -DgroupId=${pomGroupId} \
                                -DartifactId=${pomArtifact} -Dversion=${pomVersion}  \
                                -Dpackaging=${pomPackaging} -DrepositoryId=maven-hosted  -DproxySet=false \
                                -Durl=http://10.67.47.94:30083/repository/maven-hosted/  -X 
        """
}

//制品晋级
def ArtifactUpdate(updateType,artifactUrl){

    //晋级策略
    if ("${updateType}" == "snapshot -> release"){
        println("snapshot -> release")

        //下载原始制品
        sh "  rm -fr updates && mkdir updates && cd updates && wget ${artifactUrl} && ls -l "

        //获取artifactID 
        
        artifactUrl = artifactUrl -  "http://10.67.47.92:8081/repository/maven-hosted/"
        artifactUrl = artifactUrl.split("/").toList()
        
        println(artifactUrl.size())
        env.jarName = artifactUrl[-1] 
        env.pomVersion = artifactUrl[-2].replace("SNAPSHOT","RELEASE")
        env.pomArtifact = artifactUrl[-3]
        pomPackaging = artifactUrl[-1]
        pomPackaging = pomPackaging.split("\\.").toList()[-1]
        env.pomPackaging = pomPackaging[-1]
        env.pomGroupId = artifactUrl[0..-4].join(".")
        println("${pomGroupId}##${pomArtifact}##${pomVersion}##${pomPackaging}")
        env.newJarName = "${pomArtifact}-${pomVersion}.${pomPackaging}"
        
        //更改名称
        sh " cd updates && mv ${jarName} ${newJarName} "
        
        //上传制品
        env.repoName = "maven-release"
        env.filePath = "updates/${newJarName}"
        NexusUpload()
    }
}


def main(uploadType){
    GetGav()
    if ("${uploadType}" == "maven"){
        MavenUpload()
    } else if ("${uploadType}" == "nexus") {
        env.repoName = "maven-hosted"
        env.filePath = "target/${jarName}"
        NexusUpload()
    }
}