pipeline {
  agent {
    label "infra"
  }
  tools {
    maven 'Maven_3.6.3'
    jdk 'jdk-1.8.0'
  }
	options {
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
		disableConcurrentBuilds()
		timestamps()
    ansiColor('xterm')

	}

  environment {
      //ART = credentials("jfrog-jenkins-user")
      //NEXUS = credentials(" coral-nexus-user")
      //ARTIFACTORY = "https://ladbrokescoral.jfrog.io/ladbrokescoral/"
      ARTIFACTORY = credentials("jfrog-creds")
  }
  stages {

    stage("Build"){
      /*agent {
        docker {
          image "368130942539.dkr.ecr.eu-west-2.amazonaws.com/base-images/maven:latest"
          label "infra"
        }
      }*/
      // use a docker image to build artefacts like a Docker Image and App manifest
      steps {
        //withCredentials([string(credentialsId: 'sonar_token', variable: 'SONARTOKEN')]) {
          sh "ci/build.sh"
        //}
        stash includes: '**/target/*.jar', name: 'app'
      }
    }
    stage("Test Manifest"){
      agent {
        docker {
          image "lcgomnia-docker-local.dev.docker.env.works/base-images/terragrunt:latest"
          label "infra"
        }
      }
      steps {
          sh "jsonschema-2 -i manifest.json ci/manifest-schema.json"
      }
    }
    stage("Test App"){
      /*agent {
        docker {
          image "368130942539.dkr.ecr.eu-west-2.amazonaws.com/base-images/maven:latest"
          label "infra"
        }
      }*/
      // use built artefacts to and test them
      steps {
          unstash 'app'
          sh "ci/test.sh"
//          junit "**/*-reports/*.xml"
      }
    }
    stage("Dependency Check - findbugs report") {
        steps {
             script {
               last_started = env.STAGE_NAME
             }
             unstash 'app'
             //dependencyCheckPublisher canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: '', canRunOnFailed: true
             //jacoco(path: '**/target/jacoco.exec')
             jacoco(execPattern: '**/target/jacoco.exec')
             //junit testResults: '**/target/*-reports/TEST-*.xml'

        }
    }  
    stage("Publish"){
      agent {
          label "infra"
      }

      // publish Docker image to repo
      // publish App manifest
      // publish jar
      steps {
          unstash 'app'
          sh "ci/omnia-adapter.sh"
      }
    }
    // stage ("fortify upload") {
      
      // steps {
        // fortifyClean addJVMOptions: '-64', buildID: '$JOB_NAME', debug: true, logFile: '', maxHeap: '', verbose: true
        // fortifyTranslate addJVMOptions: '', buildID: '$JOB_NAME', excludeList: '**/src/test/**/*.java', logFile: './$JOB_NAME-translation.log', maxHeap: '', projectScanType: fortifyJava(javaAddOptions: '', javaClasspath: '', javaSrcFiles: '**/src/**/*.java', javaVersion: '1.8')
        // fortifyScan addJVMOptions: '-64', addOptions: '', buildID: '$JOB_NAME', customRulepacks: '', logFile: './$JOB_NAME-scan.log', maxHeap: '8000', resultsFile: './$JOB_NAME.fpr'
        //fortifyUpload appName: 'sampleapplication', appVersion: '1.0', failureCriteria: '[fortify priority order]:critical OR high', filterSet: '', pollingInterval: '', resultsFile: ''
        //fortifyUpload appName: 'sampleapplication', appVersion: 'sampleapplication', failureCriteria: '', filterSet: '', pollingInterval: '', resultsFile: './${JOB_NAME}-${BUILD_NUMBER}-results.fpr'
       // sh "ci/fortify.sh"
     // }
   // }
  }
}
