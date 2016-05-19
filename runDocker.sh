#!/bin/bash
## If ypu want to change the subnet_IP and the version_VER
# modify them olso in the  perl script

IP_BASH='10.0.0'
VER_BASH='1.0'

perl -e 'use strict;
use warnings;

my $N = 4;
my $IP = "10.0.0";
my $VER = "1.0";

for my $i (1..$N){
    `docker network disconnect pad-net node$i`;
}
`docker network disconnect pad-net cli`;

my $resRm = `docker network rm pad-net`;

print $resRm;
my $res = `docker network create --subnet=$IP.0/16 --gateway=$IP.254 pad-net`;
print "created Network : $res";

for my $i (1..$N){
  $res = `docker stop node$i`;
  print "Stopped: $res";
  $res = `docker rm node$i`;
  print "Removed container: $res";
  $res = `docker run -d --name node$i  --net pad-net --ip $IP.$i padfs/core:$VER -ip $IP.$i -id node$i $IP.2:node2`;
  print "Created container : $res";
}



$res = `docker stop cli` ;
print "Stopped client: $res";
$res = `docker rm cli`;
print "Remove Client container: $res ";
'

docker run -it --name cli --net pad-net --ip $IP_BASH.253 padfs/cli:$VER_BASH -ip $IP_BASH.253 -id client $IP_BASH.2:node2