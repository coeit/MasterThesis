#!/usr/bin/perl
use strict;
use warnings;
use Data::Dumper;
use List::Util qw( min max );

my ($max,$min,$maxIndex,$minIndex,$maxDiff,$TH,$zTH,$arrCnt,$Pos,$abs,$THsw,$windowsize,$Acc,$startIndex, $len,$position,$CutPos,$TH2,$THidx);
my (@lines,@values,@MinMax,@absArr,@Mindex,@PosArr,@newVal);

open(IN, $ARGV[0]) or die $!;
@lines = <IN>;
close IN;
open(OUT, ">", "WINDOW_OUT/".(split("/",$ARGV[0]))[1].".MinMax_window") or die $!;
open(POSITIVE_OUT, ">", "MINMAX_POS/".(split("/",$ARGV[0]))[1].".positive_CutPos.list") or die $!;
open(NEGATIVE_OUT, ">", "MINMAX_NEG/".(split("/",$ARGV[0]))[1].".negative.list") or die $!;
$windowsize = $ARGV[1];

print OUT "##############################################################\n";
print OUT "Windowsize = $windowsize\nTH =".'(max(@values) - min(@values)) * (4/5)'."
sTH = ".'(max(@values) - min(@values))*(1/10)'."
zTH = ".'int(max(@values)*0.05)'."\n";
print OUT "##############################################################\n";

foreach(@lines){
	#print $_;
	chomp;
	$Acc = (split("::",$_))[0];
	$_ =~ s/\s//g;
	@values = split(",",(split("::",$_))[1]);
	$TH = (max(@values) - min(@values)) * (4/5);
	$zTH = int(max(@values)*0.05);
	print OUT $Acc."::";
	my @maxDiffarr;
	#print $Acc."\n";
	unshift(@values, min(@values));
	for(my $i = 0; $i < scalar(@values) - $windowsize; $i = $i + 1){
		$max = 0;
		$min = $values[$i];
		$maxIndex = 0;
		$minIndex = 0;
		for(my $j = 0; $j < $windowsize; $j++){
			if($values[$i+$j] > $max){
				$max = $values[$i+$j];
				$maxIndex = $i+$j;
			}
			if($values[$i+$j] < $min){
				$min = $values[$i+$j];
				$minIndex = $i+$j;
			}
		}
		if($maxIndex < $minIndex){
			$maxDiff = $min - $max;
		}
		else{
			$maxDiff = $max - $min;
		}
		if(abs($maxDiff) <= $zTH){
			$maxDiff = 0;
		}
		push (@maxDiffarr,$maxDiff);
	}
	print OUT join(", ",@maxDiffarr)."::";


	$arrCnt = 0;
	$Pos = 1;
	my @signChIndex;
	for(my $i = 0; $i < scalar(@maxDiffarr); $i++){
		if($Pos && $maxDiffarr[$i] < 0){
			$Pos = 0;
			#print $i.", ";
			push(@signChIndex, $i);
		}
		if(!$Pos && $maxDiffarr[$i] > 0) {
			$Pos = 1;
			#print $i.", ";
			push(@signChIndex, $i);
		}
	}
	push(@signChIndex,$#maxDiffarr+1);;
	$startIndex = 0;
	my @tmp;
	my @signSplit;
	foreach(@signChIndex) {
		for(my $i = $startIndex; $i < $_; $i++){
			push(@tmp, $maxDiffarr[$i]);
		}
		push(@signSplit, [@tmp]);
		$startIndex = $_;
		@tmp = ();
	}


	@PosArr = ();
	@absArr = ();
	$position = 0;
	my $sTH = (max(@values) - min(@values))*(1/10);
	for(my $i = 0; $i < scalar(@signSplit)-1; $i++){
		$min = min(@{$signSplit[$i]});
		#print OUT "\n***$min***\n";
		#print OUT $position."-";
		if( $min < 0){
			$max = max(@{$signSplit[$i+1]});
			$abs = abs($min) + $max;
			if($min < $sTH * (-1) && $max > $sTH){
				push(@absArr, $abs);
				push(@PosArr,$position."-".($position + scalar(@{$signSplit[$i+1]}) + scalar(@{$signSplit[$i]})));
			}
			#push(@PosArr, $position + scalar(@{$signSplit[$i+1]}) + scalar(@{$signSplit[$i]}));
		}
		$position += scalar(@{$signSplit[$i]});
	}
	#print OUT join(", ",@absArr)."::";
	my @idx = sort { $absArr[$b] <=> $absArr[$a] } 0 .. $#absArr;

	@absArr = @absArr[@idx];
	@PosArr = @PosArr[@idx];
	print OUT join(", ",@absArr)."::";
	print OUT join(", ",@PosArr)."::";
	print OUT "TH ".$TH."::sTH ".$sTH."::zTH ".$zTH."\n";
	my @positiveIdx;
	$THidx = 0;
	foreach(@absArr){
		if($_ >= $TH){
			push(@positiveIdx, $THidx);
		}
		$THidx++;
	}
	my @PosSplit;
	my @all_Cut_pos;
	#print OUT scalar(@positiveIdx)."\n";
	if(scalar(@positiveIdx) <= 2 && scalar(@positiveIdx) != 0){
		print POSITIVE_OUT $Acc."::";
		foreach(@positiveIdx){
			@Mindex = ();
			@newVal = ();
			@PosSplit = split("-",$PosArr[$_]);
			for(my $i = $PosSplit[0]; $i < $PosSplit[1]; $i++){
				push (@newVal,$values[$i]);
			}
			$min = min(@newVal);
			#print OUT $min."\n";
			for(my $i = $PosSplit[0]; $i < $PosSplit[1]; $i++){
				if($values[$i] == $min){
					push (@Mindex,$i);
				}
			}
			#$CutPos = int((min(@Mindex)+max(@Mindex))/2);
			$CutPos = $Mindex[int(scalar(@Mindex) / 2)];
			push(@all_Cut_pos,$CutPos);
		}
		print POSITIVE_OUT join(",",sort { $a <=> $b } @all_Cut_pos)."\n";
	} else {
		print NEGATIVE_OUT $Acc."\n";
	}
	@maxDiffarr = ();
	@signChIndex = ();
	@signSplit = ();
	@MinMax = ();
	@absArr = ();
}

close OUT;
close POSITIVE_OUT;
close NEGATIVE_OUT;
