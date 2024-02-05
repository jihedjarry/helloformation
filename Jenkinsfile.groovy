pipeline{
	environment {
		IMAGE_NAME = "alpinehelloworld"
		IMAGE_TAG = "latest"
		registry = "192.168.1.38:5000/myapp"
		registryCredential = 'myregistry_login'
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
		
		stage('Deploy our image') {
			steps{
				script {
					docker.withRegistry( 'http://192.168.1.38:5000', registryCredential ) {
                				def customImage = docker.build("$registry:${IMAGE_TAG}")
                        			customImage.push()
					}
         	        	}
                	}	
		}
	}
}
