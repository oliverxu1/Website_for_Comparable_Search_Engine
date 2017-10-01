<?php ini_set('memory_limit', '3000M');
include 'SpellCorrector.php';
header('Content-Type: text/html; charset=utf-8');
$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

$csv = array_map('str_getcsv', file('Apache/Solr/LATimesData/mapLATimesDataFile.csv'));
$map = array();
foreach ($csv as $value) {
	$temp = "/home/ziao/Downloads/solr-6.5.0/LATimesDownloadData/" . $value[0];	
	$map[$temp] = $value[1];
}
if ($query)
{
	if (!isset($_GET["Ins"])) {
		$arr = explode(" ", $query);
		$size = sizeof($arr);
		$newquery = SpellCorrector::correct($arr[0]);
		for ($i = 1; $i < $size; $i++) {
			$newquery = $newquery . " " . SpellCorrector::correct($arr[$i]);		
		}
		if (strcmp(strtolower($newquery), strtolower($query)) != 0) {
			echo "Now you are searching " . $newquery . " instead of ";				
			echo "<a href=http://localhost/index.php?" . $_SERVER['QUERY_STRING'] . "&Ins=1>".$query."</a>";
		}
		$query = $newquery;
	}
	
 // The Apache Solr Client library should be on the include path
 // which is usually most easily accomplished by placing in the
 // same directory as this script ( . or current directory is a default
 // php include path entry in the php.ini)
 require_once('Apache/Solr/Service.php');
 // create a new solr service instance - host, port, and corename
 // path (all defaults in this example)
 $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');
 // if magic quotes is enabled then stripslashes will be needed
 if (get_magic_quotes_gpc() == 1)
 {
 $query = stripslashes($query);
 }
 // in production code you'll always want to use a try /catch for any
 // possible exceptions emitted by searching (i.e. connection
 // problems or a query parsing error)
 try
 {
 	if (isset($_GET["PR"])) {
 		$results = $solr->search($query, 0, $limit, array('sort' => 'pageRankFile desc'));
 	} else {
 		$results = $solr->search($query, 0, $limit);
 	} 	
 }
 catch (Exception $e)
 {
 // in production you'd probably log or email this error to an admin
 // and then show a special message to the user but for this example
 // we're going to show the full exception
 die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
 }
}
?>
<html>
 <head>
 <title>PHP Solr Client Example</title>
  <script src="//code.jquery.com/jquery-1.9.1.js"></script>
  <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
  <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">  
 </head>
 <body>
 <form accept-charset="utf-8" method="get">
 <label for="q">Search:</label>
 <input id="q" name="q" type="text" onkeyup="auto()" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
 <input type="submit"/>
 <input type="checkbox" name="PR" <?php if(isset($_GET['PR'])) echo "checked='checked'"; ?>> Use PageRank
 </form>
 <p id="demo"></p>
<?php
// display results
if ($results)
{
 $total = (int) $results->response->numFound;
 $start = min(1, $total);
 $end = min($limit, $total);
?>
 <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
 <ol>
<?php
 // iterate result documents
 foreach ($results->response->docs as $doc)
 { 	
?>
 <li>
 <table>

<tr>
	<td>
	<a href="<?php echo($map[$doc->id]) ?>" target="_blank">
		<?php echo htmlspecialchars($doc->title, ENT_NOQUOTES, 'utf-8'); ?>
	</a>		
	</td>
</tr>
<tr>
	<td>
	<a href="<?php echo($map[$doc->id]) ?>" target="_blank">
		<?php echo htmlspecialchars($map[$doc->id], ENT_NOQUOTES, 'utf-8'); ?>
	</td>
</tr>
<tr>
	<td>
		<?php echo htmlspecialchars($doc->id, ENT_NOQUOTES, 'utf-8'); ?>
	</td>
</tr>
<tr>
	<td>
		<?php echo htmlspecialchars($doc->description, ENT_NOQUOTES, 'utf-8'); ?>
	</td>
</tr>
<tr>
	<td>Snippet:
		<?php
			$text = strip_tags(file_get_contents($doc->id),'<script><style>');
			$text = preg_replace('/<script\b[^>]*>(.*?)<\/script>/is', "", $text); 
			$text = preg_replace('/<style\b[^>]*>(.*?)<\/style>/is', "", $text); 
			$text = explode('.', $text);			
			foreach ($text as $key) {	
				if (strlen(strstr(strtolower($key), $query)) > 0) {
					echo $key;
					break;
				}				
			}
		?>
	</td>
</tr>
 </table>
 </li>
<?php
 }
?>
 </ol>
<?php
}
?>
	<script src="script.js"></script>
 </body>
</html>