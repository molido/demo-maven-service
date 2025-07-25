package org.devops

//saltstack
def saltDeploy(hosts, func){
    sh "salt \"${hosts}\" ${func} "
}

//ansible

def ansibleDeply(hosts, func){
    def ansibleHome = tool name: 'ansible'
    sh "${ansibleHome}/bin/ansible ${func} ${hosts}"
}