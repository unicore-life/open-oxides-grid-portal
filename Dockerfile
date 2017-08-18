FROM centos:7

MAINTAINER "Rafal Kluszczynski" <r.kluszczynski@icm.edu.pl>

RUN curl -O http://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-5.noarch.rpm
RUN rpm -ivh epel-release-7-5.noarch.rpm

ADD src/docker /docker-files

RUN cp /docker-files/eugridpma.repo /etc/yum.repos.d

RUN yum -y update && yum clean all
RUN yum -y install java-1.8.0-openjdk
RUN yum -y install java-1.8.0-openjdk-devel
RUN yum -y install ntp
RUN yum -y install ca_policy_eugridpma fetch-crl

RUN systemctl enable ntpd.service ntpdate.service fetch-crl-boot.service fetch-crl-cron.service
RUN chkconfig ntpdate on
RUN chkconfig ntpd on
RUN chkconfig fetch-crl-boot on
RUN chkconfig fetch-crl-cron on

ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk

ADD ./build/install/oxides-grid-portal /oxides-grid-portal
RUN cd /oxides-grid-portal

EXPOSE 443

CMD ["./bin/oxides-grid-portal"]

# ReadIt: http://developerblog.redhat.com/2014/05/05/running-systemd-within-docker-container/
