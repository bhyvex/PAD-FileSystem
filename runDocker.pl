use strict;
use warnings;
my $N = shift || 4;
my $IP = shift || '172.19.0';
my $VER = '1.0';

my $res = `docker network create --subnet=$IP.0/16 pad-net`;
print $res;

for my $i (1..$N){
  $res = `docker stop node$i`;
  print $res;
  $res = `docker rm node$i`;
  print $res;
  $res = `docker run --name node$i --net pad-net --ip $IP.$i  padfs/core:$VER com.dido.pad.PadFsNode -ip $IP.$i -id node$i $IP.1:node1`;
  print $res;
}


$res = `docker stop cli` ;
print $res;
$res = `docker rm cli`;
print $res;
$res = `docker run --name cli --net pad-net --ip $IP.254 padfs/cli:$VER  com.dido.pad.cli.MainClient -ip $IP.254 -id client $IP.1:node1` ;
print $res;