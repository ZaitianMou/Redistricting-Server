#!/usr/bin/env bash

#SBATCH --job-name=redistrict_eagles
#SBATCH --output=redistrict.log
#SBATCH --ntasks-per-node=24
#SBATCH --nodes=8
#SBATCH --time=02:00:00
#SBATCH -p short-24core

echo $SLURM_JOB_ID

cat << EOF > algorithm.py

import json
import random
from networkx import *
import time
import sys


# CONTROLS
file = ""

# INPUTS
iterationLimit = 500
requestedNumOfDistricts = 14

populationVariation = 0.05
compactnessThreshold = 0.45

# CACULATED CONSTANTS
idealPopulation = 0
populationLB = 0
populationUB = 0

# VALUES
precinctList = {}
clusterList = {}

class Precinct():
    def __init__(self, data):
        self.__dict__ = data
        self.__NEIGHBORS = list(map(int, self.__dict__["NEIGHBORS"].split(",")))

    def getNeighbors(self):
        return self.__NEIGHBORS

    def setNeighbors(self, x):
        self.__NEIGHBORS = x

class Cluster():
    def __init__(self, clusterID=None, precincts=None, neighbors=None):
        self.id = clusterID
        self.precincts = [precincts]
        self.neighbors = neighbors
        self.HVAP = 0
        self.WVAP = 0
        self.BVAP = 0
        self.AMINVAP = 0
        self.ASIANVAP = 0
        self.NHPIVAP = 0

    def getRandNeighbor(self):
        keys = list(clusterList.keys())
        rand = random.choice(self.neighbors)
        while rand not in keys:
            rand = random.choice(self.neighbors)
        return clusterList[rand]

    def combineCluster(self, neighbor):
        self.precincts = self.precincts + neighbor.precincts  # merge list of precincts
        self.id = self.precincts[0]  # set primary id as self's first id

        self.neighbors = list(set(self.neighbors + neighbor.neighbors))  # merge list of neighbors, remove repeats
        self.neighbors = list(set(self.neighbors) - set(self.precincts))  # remove self precincts

        keys = clusterList.keys()
        self.neighbors = list(keys & set(self.neighbors))  # intersection with remaining keys in the clusterlist

        updateNeighbors(self)

        del clusterList[neighbor.id]  # remove neighbor from clusterlist


def updateNeighbors(cluster):
    # update neighbors for neighboring clusters
    temp = cluster.precincts[1:]  # excluding first value (id)
    for neighborID in cluster.neighbors:
        onNeighbor = clusterList[neighborID]  # locate cluster obj

        if cluster.id not in onNeighbor.neighbors:  # add primary id if neighbors don't have it
            onNeighbor.neighbors = onNeighbor.neighbors + [cluster.id]

        onNeighbor.neighbors = list(set(onNeighbor.neighbors) - set(temp))  # remove nonprimary ids from neighbor


def createNDistricts(requestedNumOfDistricts):
    while len(clusterList) != requestedNumOfDistricts:
        cluster = random.choice(list(clusterList.values()))  # select random cluster
        neighbor = cluster.getRandNeighbor()
        cluster.combineCluster(neighbor)


def getClusterPopulation(cluster):
    clusterPopulation = 0
    for precinctID in cluster:  # for each node, get its population
        precinct = precinctList[precinctID]  # locate the precinct object
        clusterPopulation += precinct.TOTPOP
    return clusterPopulation


def isBorderNode(precinctID,subgraph):
    precinct = precinctList[precinctID]  # locate precinct obj
    for neighborID in precinct.getNeighbors():  # for each neighbor of the precinct
        if neighborID not in subgraph:  # if the neighbor is not part of the cluster
            # if any(neighbor not in subgraph for neighbor in precinct.getNeighbors()):
            return 1
    return 0


def getCompactnessScore(subgraph):  # to scale (0-1) where 0 is best score
    borderNodesCount = 0
    for precinctID in subgraph:  # for each precinct in the cluster
        borderNodesCount += isBorderNode(precinctID, subgraph)
    score = borderNodesCount / len(subgraph)
    return score


def isAcceptable(popScore, comScore):
    # condition 1: population
    if popScore < populationLB or popScore > populationUB:  # if percentage not in within range
        return False
    # condition 2: compactness
    if comScore > compactnessThreshold:
        return False
    return True


def selectClusterNeighborPair():
    # combine random cluster and neighbor
    cluster = random.choice(list(clusterList.values()))
    rand = random.choice(cluster.neighbors)
    neighbor = clusterList[rand]

    # for later use when comparing oldAcceptableScore to new AcceptableScore
    populationDifference = abs(getClusterPopulation(cluster.precincts) - getClusterPopulation(neighbor.precincts))
    compactnessDifference = getCompactnessScore(cluster.precincts) + getCompactnessScore(neighbor.precincts)

    #print("Redistricting Cluster %d and %d" % (cluster.id, neighbor.id))
    combined = combineClusters(cluster, neighbor)
    return combined, neighbor, populationDifference, compactnessDifference


def combineClusters(cluster, neighbor):
    newCluster = Cluster()  # declare new Cluster obj
    newCluster.precincts = cluster.precincts + neighbor.precincts  # merge list of precincts
    newCluster.id = cluster.precincts[0]  # set primary id as self's first id
    return newCluster


def findEdgeList(cluster):
    edgeList = []
    clusterPrecincts = cluster.precincts
    for precinctID in clusterPrecincts:  # for each precinct in the cluster
        precinct = precinctList[precinctID]  # locate precinct obj
        for neighborID in precinct.getNeighbors():  # for each neighbor in the precinct
            if neighborID in clusterPrecincts:  # if the neighbor is part of the cluster
                edgeList.append(tuple((precinct.ID, neighborID)))
    edgeList = list(set(tuple(sorted(edge)) for edge in edgeList))  # remove duplicate pairs
    return edgeList


def generateSpanningTree(cluster):
    #print("\nGenerating spanning tree...")
    graph = nx.Graph()
    edgesList = findEdgeList(cluster)
    for edge in edgesList:
        graph.add_edge(edge[0], edge[1], weight=random.random())

    tree = nx.minimum_spanning_tree(graph)
    return tree


def compareAcceptability(subgraphOnePop, subgraphOneCom, subgraphTwoPop, subgraphTwoCom, oldCompactnessDifference, oldPopulationDifference):
    # calculate differences in the new subgraphs
    newPopulationDifference = abs(subgraphOnePop - subgraphTwoPop)
    newCompactnessDifference = subgraphOneCom + subgraphTwoCom

    subgraphsPopulationSum = subgraphOnePop + subgraphTwoPop

    # to balance the weight of compactnessScore (0-1) to populationScore
    oldCompactnessDifference *= subgraphsPopulationSum
    newCompactnessDifference *= subgraphsPopulationSum

    oldAcceptabilityScore = oldPopulationDifference*2 + oldCompactnessDifference
    newAcceptabilityScore = newPopulationDifference*2 + newCompactnessDifference

    if newAcceptabilityScore <= oldAcceptabilityScore: # and newCompactnessDifference <= oldCompactnessDifference:
        return newAcceptabilityScore
    return None


def findValidEdges(tree, oldPopulationDiff, oldCompactnessDiff):
    randomEdges = random.sample(list(tree.edges()), len(tree.edges()))  # randomize order of edges

    currentAcceptableEdge = None
    currentAcceptableScore = None

    for edge in randomEdges:  # for every tree edge
        tree.remove_edge(edge[0], edge[1])  # split into 2 graphs
        subgraphs = sorted(nx.connected_components(tree))
        tree.add_edge(edge[0], edge[1]) # reset tree

        # CALCULATE POPULATIONS AND COMPACTNESS SCORES
        subgraphOnePop = getClusterPopulation(subgraphs[0])
        subgraphTwoPop = getClusterPopulation(subgraphs[1])
        subgraphOneCom = getCompactnessScore(subgraphs[0])
        subgraphTwoCom = getCompactnessScore(subgraphs[1])

        if isAcceptable(subgraphOnePop, subgraphOneCom) and isAcceptable(subgraphTwoPop, subgraphTwoCom):  # if both are valid
            return edge

        # else compare scores, None returned if score is worst
        newScore = compareAcceptability(subgraphOnePop, subgraphOneCom, subgraphTwoPop, subgraphTwoCom, oldCompactnessDiff, oldPopulationDiff)
        if newScore is not None:  # if newScore exists
            if currentAcceptableScore is None or newScore < currentAcceptableScore:  # if newScore is better
                currentAcceptableScore = newScore
                currentAcceptableEdge = edge

    if currentAcceptableEdge is not None:  # if Acceptable edge exists
        return currentAcceptableEdge

    return None  # return none if no valid edges are found


def cutEdge(spanningTree, edge):
    spanningTree.remove_edge(edge[0], edge[1])
    subgraphs = list(sorted(nx.connected_components(spanningTree)))

    # creating new cluster objects for the new subgraphs
    newSubgraphOne = Cluster(list(subgraphs[0])[0])
    newSubgraphTwo = Cluster(list(subgraphs[1])[0])
    newSubgraphOne.precincts = list(subgraphs[0])
    newSubgraphTwo.precincts = list(subgraphs[1])

    # add clusters to clusterList
    clusterList[newSubgraphOne.id] = newSubgraphOne
    clusterList[newSubgraphTwo.id] = newSubgraphTwo

    # update cluster's neighborlist
    updateCluster(clusterList[newSubgraphOne.id])
    updateCluster(clusterList[newSubgraphTwo.id])

    # update the neighbor's neighborlist
    updateClusterNeighbors(clusterList[newSubgraphOne.id])
    updateClusterNeighbors(clusterList[newSubgraphTwo.id])


def updateCluster(cluster):
    neighbors = []  # union of neighbors of all precincts in the subgraph
    for ID in cluster.precincts:
        precinct = precinctList[ID]  # locate precinct
        neighbors += precinct.getNeighbors()
    neighbors = list(set(neighbors) - set(cluster.precincts))  # remove self precincts from neighborlist

    # for each neighbor precinct, check if it is a primary ID, else find its primary ID
    for clusters in clusterList.values():
        precincts = clusters.precincts
        if set(precincts) & set(neighbors):  # if there is a common value in current neighbors list and each cluster's precincts
            neighbors.append(clusters.id)

    keys = clusterList.keys()
    neighbors = list(keys & set(neighbors))  # intersection of neighborID with the keyID in the clusterlist

    cluster.neighbors = list(set(neighbors))  # use set to remove duplicates


def updateClusterNeighbors(cluster):
    for neighborID in cluster.neighbors:
        neighbor = clusterList[neighborID]  # locate neighbor obj
        if cluster.id not in neighbor.neighbors:
            neighbor.neighbors = neighbor.neighbors + [cluster.id]


if __name__ == '__main__':
    file = sys.argv[1]
    populationVariation = float(sys.argv[2])
    compactnessThreshold = float(sys.argv[3])
    start_time = time.time()
    data = ''
    with open(file, "r") as read_file:
        data = json.load(read_file)

    # READ JSON
    precinctsData = data["features"]
    for data in precinctsData:
        properties = data["properties"]
        precinctObj = Precinct(properties)
        precinctList[precinctObj.ID] = precinctObj

        clusterObj = Cluster(precinctObj.ID, precinctObj.ID, precinctObj.getNeighbors())  # store its id and neighbors into cluster object
        clusterList[precinctObj.ID] = clusterObj

        # find total population to get ideal population
        idealPopulation += precinctObj.TOTPOP

    # CALCULATING CONSTANTS
    idealPopulation = idealPopulation / requestedNumOfDistricts
    populationUB = (1 + (populationVariation / 2)) * idealPopulation  # upperbound
    populationLB = (1 - (populationVariation / 2)) * idealPopulation  # lowerbound

    # START ALGORITHM - CREATE N DISTRICTS
    createNDistricts(requestedNumOfDistricts)

    # START REDISTRICTING LOOP
    iterationCount = 0
    while iterationCount != iterationLimit:
        newCluster, neighbor, populationDifference, compactnessDifference = selectClusterNeighborPair()
        tree = generateSpanningTree(newCluster)
        validEdge = findValidEdges(tree, populationDifference, compactnessDifference)  # returns one edge

        if validEdge is None:
            continue

        # remove selected cluster and neighbor for new pair to be added
        del clusterList[newCluster.id]
        del clusterList[neighbor.id]
        # remove selected cluster and neighbor id from all neighbor values
        for cluster in clusterList.values():
            cluster.neighbors = list(set(cluster.neighbors) - set((newCluster.id, neighbor.id)))

        cutEdge(tree, validEdge)
        iterationCount += 1

    # SUMMARIZING RESULTS (bloxplot VAP data)
    for district in clusterList.values():
        for precinctID in district.precincts:
            precinct = precinctList[precinctID]
            district.BVAP += precinct.__dict__["BVAP"]
            district.HVAP += precinct.__dict__["HVAP"]
            district.WVAP += precinct.__dict__["WVAP"]
            district.AMINVAP += precinct.__dict__["AMINVAP"]
            district.ASIANVAP += precinct.__dict__["ASIANVAP"]
            district.NHPIVAP += precinct.__dict__["NHPIVAP"]

    #RESULTS TO JSON
    districts = [{"districtNumber": index, "HVAP": district.HVAP, "WVAP": district.WVAP, "BVAP": district.BVAP,
                "AMINVAP": district.AMINVAP, "ASIANVAP": district.ASIANVAP, "NHPIVAP": district.NHPIVAP,
                "precincts": district.precincts} for index, district in enumerate(clusterList.values(), start=1)]
    districtingPlan = {"districts": districts}
    jsonStr = json.dumps(districtingPlan, indent=3)
    print(jsonStr+',')


EOF

