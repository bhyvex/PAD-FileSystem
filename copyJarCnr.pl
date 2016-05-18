use strict;
use Net::SCP;

# novello 146.48.82.73

# node1 146.48.82.91  node1.novello.isti.cnr.it
# node2 146.48.82.84
# node3 146.48.82.75
# node4 146.48.82.76
# node5 146.48.82.93  (client)

my %servers = (
    "node1"  => "146.48.82.91",
    "node2"  => "146.48.82.84",
    "node3"  => "146.48.82.75",
    "node4"  => "146.48.82.76",
    "node5"  => "146.48.82.93"
);

foreach my $key (keys %servers){
  my $scp = Net::SCP->new( "$key.novello.isti.cnr.it", "aspirantidottori" );
  # $scp->cwd();
  `echo 'java -cp core-1.0-jar-with-dependencies.jar com.dido.pad.PadFsNode -ip $servers{$key} -id $key 146.48.82.91:node1 146.48.82.84:node2' > start.sh`;
  `chmod +x start.sh`;

  `echo 'rm /tmp/node*' > eraseData.sh`;
  `chmod +x eraseData.sh`;

  $scp->put('eraseData.sh') or die $scp->{errstr};

  $scp->put('start.sh') or die $scp->{errstr};
  $scp->put("core/target/core-1.0-jar-with-dependencies.jar") or die $scp->{errstr};
  print ("Loaded PadFsNode into $key \n");
}

my $scp = Net::SCP->new( "node5.novello.isti.cnr.it", "aspirantidottori" );
#$scp->cwd('luca');
`echo 'java -cp cli-1.0-jar-with-dependencies.jar com.dido.pad.cli.MainClient  -ip 146.48.82.93 -id client 146.48.82.91:node1' > start-client.sh`;
`chmod +x start-client.sh`;
 $scp->put('start-client.sh') or die $scp->{errstr};
$scp->put("cli/target/cli-1.0-jar-with-dependencies.jar") or die $scp->{errstr};
print ("Loaded Client into node5 \n");

`rm start.sh && rm start-client.sh && rm eraseData.sh``


