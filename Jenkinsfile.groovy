pipeline{
	environment {
		IMAGE_NAME = "alpinehelloworld"
		IMAGE_TAG = "latest"
		STAGING = "jarryjihed-staging"
		PRODUCTION = "jarryjihed-production"		

	}
	agent none

	stages {
		stage('Build image') {
			agent any
			steps {
				script {
				sh 'docker build -t jihedjarry51/${IMAGE_NAME}:${IMAGE_TAG} .'

			
				}
			}	
		}
		
		stage('Run container based on builded image') {
                        agent any
                        steps {
                                script { 
                                sh '''
					docker run -d -p 80:5000 -e PORT=5000 --name ${IMAGE_NAME} jihedjarry51/${IMAGE_NAME}:${IMAGE_TAG}
					sleep 5
				'''
				}
                        }       
                }
		
		stage('Test image') {
                        agent any
                        steps {
                                script {
                                sh '''                                                        
                                        curl http://localhost | grep -q "Hello world!"
                                ''' 
                                }
                        }
                }

		stage('Clean container') {
                        agent any
                        steps {
                                script {
                                sh '''
                                        docker stop ${IMAGE_NAME}
					docker rm ${IMAGE_NAME}
                                '''
                                }
                        }
                }

		def imageName_Registry='192.168.1.64:5000/myapp'

    		stage('DOCKER - Build/Push registry'){
      			docker.withRegistry('http://192.168.1.64:5000', 'myregistry_login') {
			def customImage = docker.build("$imageName_Registry:${IMAGE_TAG}")
        		customImage.push()
 		}
      		sh "docker rmi $imageName_Registry:${IMAGE_TAG}"
    		}

		/* Docker - test */
    		stage('DOCKER - check registry'){
      			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'myregistry_login',usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      			sh 'curl -sk --user $USERNAME:$PASSWORD https://192.168.1.64:5000/v2/myapp/tags/list'
      	 		}
		}
	


	}

}
