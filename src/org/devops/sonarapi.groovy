package org.devops


//封装HTTP

def HttpReq(reqType,reqUrl,reqBody){
    def sonarServer = "http://10.67.47.92:30090/api"
   
    def result = httpRequest authentication: 'admin',
            httpMode: reqType, 
            contentType: "APPLICATION_JSON",
            consoleLogResponseBody: true,
            ignoreSslErrors: true, 
            requestBody: reqBody,
            url: "${sonarServer}/${reqUrl}"
            //quiet: true
    
    return result
}


//获取Sonar质量阈状态
def GetProjectStatus(projectName){
    def apiUrl = "project_branches/list?project=${projectName}"
    def response = HttpReq("GET",apiUrl,'')
    
    response = readJSON text: """${response.content}"""
    def result = response["branches"][0]["status"]["qualityGateStatus"]
    
    //println(response)
    
   return result
}

//获取Sonar质量阈状态(多分支)
def GetProjectStatus(projectName,branchName){
    def apiUrl = "qualitygates/project_status?projectKey=${projectName}&branch=${branchName}"
    response = HttpReq("GET",apiUrl,'')
    
    response = readJSON text: """${response.content}"""
    def result = response["projectStatus"]["status"]
    
    //println(response)
    
   return result
}

//搜索Sonar项目
def SerarchProject(projectName){
    def apiUrl = "projects/search?projects=${projectName}"
    def response = HttpReq("GET",apiUrl,'')

    response = readJSON text: """${response.content}"""
    def result = response["paging"]["total"]

    if(result.toString() == "0"){
       return "false"
    } else {
       return "true"
    }
}

//创建Sonar项目
def CreateProject(projectName){
    def apiUrl =  "projects/create?name=${projectName}&project=${projectName}"
    def response = HttpReq("POST",apiUrl,'')
    println(response)
}

//配置项目质量规则

def ConfigQualityProfiles(projectName,lang,qpname){
    println("aaaaaaa" + qpname)
    def apiUrl = "qualityprofiles/add_project?language=${lang}&project=${projectName}&qualityProfile=${qpname}"
    def response = HttpReq("POST",apiUrl,'')
    println(response)
}


// 获取质量阈 ID
def GetQualtyGateId(gateName) {
    def apiUrl = "qualitygates/list"
    def response = HttpReq("GET", apiUrl, '')
    def json = readJSON text: response.content
    def gate = json.qualitygates.find { it.name == "Sonar way" }
    if (!gate) {
        error "未找到质量阈名称：${gateName}"
    }
    return gate.id
}

//配置项目质量阈

def ConfigQualityGates(projectName,gateName){
//    def gateId = GetQualtyGateId(gateName)
//    def apiUrl = "qualitygates/select?gateId=${gateId}&projectKey=${projectName}"
//    def response = HttpReq("POST",apiUrl,'')
//    println(response)println(response)
    
    def apiUrl = "qualitygates/select?gateName=${URLEncoder.encode("Sonar way", 'UTF-8')}&projectKey=${projectName}"
    def response = HttpReq("POST", apiUrl, '')
    println(response)
}