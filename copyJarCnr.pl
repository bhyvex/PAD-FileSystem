use strict;
use Net::SCP qw(scp iscp);
use Net::SSH qw(ssh);
use Log::Dispatch::Syslog;

# novello 146.48.82.73 
# node1 146.48.82.91  node1.novello.isti.cnr.it
# node2 146.48.82.84
# node3 146.48.82.75
# node4 146.48.82.76
# node5 146.48.82.93

#declare local variables
my $scp;
my $host = "node1.novello.isti.cnr.it";
my $user = "aspirantidottori";
my $remotedir = "/home/";
my $file = "test.txt";
my $cmd = "/bin/ls";

######first connect to $host via Net::SSH and run /bin/ls###########
ssh("$user\@$host",$cmd);

######first connect to $host via Net::SSH and copy file $file###########

