FROM openjdk:8
LABEL "author"="735140144"
WORKDIR /datavines
COPY ./datavines-1.0.0-SNAPSHOT-bin.tar.gz /datavines
RUN tar -zxvf datavines-1.0.0-SNAPSHOT-bin.tar.gz
ENV TZ=Asia/Shanghai
ENV LANG=zh_CN.UTF-8
EXPOSE 5600
ENTRYPOINT ["datavines-1.0.0-SNAPSHOT-bin/bin/datavines-daemon.sh","start_container"]
