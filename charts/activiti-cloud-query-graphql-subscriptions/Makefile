CHART_REPOSITORY := $(or $(CHART_REPOSITORY),http://jenkins-x-chartmuseum:8080)
CURRENT=$(shell pwd)
NAME := $(or $(APP_NAME), $(shell basename $(CURRENT)))
OS := $(shell uname)
RELEASE_VERSION := $(or $(shell cat ../../VERSION), $(shell sed -n 's/^version: //p' Chart.yaml))

GITHUB_CHARTS_REPO := $(or $(GITHUB_CHARTS_REPO),$(shell git config --get remote.origin.url))
GITHUB_CHARTS_BRANCH := $(or $(GITHUB_CHARTS_BRANCH),gh-pages)

CHARTMUSEUM_GS_BUCKET := $(or $(CHARTMUSEUM_GS_BUCKET),$(ORG)-chartmuseum)

$(NAME)-$(RELEASE_VERSION).tgz: 
	${MAKE} package

credentials: 
	git config --global credential.helper store
	jx step git credentials
		
init:
	helm init --client-only

build: clean
	rm -rf requirements.lock
	helm dependency build
	helm lint

preview: build
	helm template .

package: build init
	helm package .

install: clean build
	helm install . --name ${NAME}

upgrade: clean build
	helm upgrade ${NAME} .

delete:
	helm delete --purge ${NAME}

clean:
	rm -rf charts
	rm -rf ${NAME}*.tgz

release: $(NAME)-$(RELEASE_VERSION).tgz
	curl --fail -u $(CHARTMUSEUM_CREDS_USR):$(CHARTMUSEUM_CREDS_PSW) --data-binary "@$(NAME)-$(RELEASE_VERSION).tgz" $(CHART_REPOSITORY)/api/charts
	@echo Released "@$(NAME)-$(RELEASE_VERSION).tgz" to repositories 

github: $(NAME)-$(RELEASE_VERSION).tgz credentials
	$(eval GITHUB_CHARTS_DIR := $(shell basename $(GITHUB_CHARTS_REPO) .git))
	git clone -b "$(GITHUB_CHARTS_BRANCH)" "$(GITHUB_CHARTS_REPO)" $(GITHUB_CHARTS_DIR)
	cp "$(NAME)-$(RELEASE_VERSION).tgz" $(GITHUB_CHARTS_DIR)
	cd $(GITHUB_CHARTS_DIR) && \
	   helm repo index . && \
	   git add . && \
	   git status && \
	   git commit -m "fix:(version) release $(NAME)-$(RELEASE_VERSION).tgz" && \
	   git push origin "$(GITHUB_CHARTS_BRANCH)"
	rm -rf $(GITHUB_CHARTS_DIR)
	
gs-bucket:
	curl --fail -L $(CHART_REPOSITORY)/index.yaml | gsutil cp - "gs://$(CHARTMUSEUM_GS_BUCKET)/index.yaml"
	
gs-bucket-docker:
	docker run -it --rm -v /root/.boto:/root/.boto google/cloud-sdk:alpine \
		bash -c "gcloud config set pass_credentials_to_gsutil false && \
			curl --fail -L ${CHART_REPOSITORY}/index.yaml | gsutil cp - gs://${CHARTMUSEUM_GS_BUCKET}/index.yaml"
	
tag:
	sed -i -e "s/version:.*/version: $(RELEASE_VERSION)/" Chart.yaml
	sed -i -e "s|repository: .*|repository: $(DOCKER_REGISTRY)\/$(ORG)\/$(APP_NAME)|" values.yaml
	sed -i -e "s/tag: .*/tag: $(RELEASE_VERSION)/" values.yaml
